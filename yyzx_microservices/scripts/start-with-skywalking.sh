#!/bin/bash
# ============================================================
# 颐养中心微服务 — SkyWalking Agent 启动脚本 (Linux/Mac)
# ============================================================
# 使用方法:
#   chmod +x start-with-skywalking.sh
#   export SKYWALKING_AGENT_HOME=/opt/skywalking-agent  # 可选，默认 ./skywalking-agent
#   ./start-with-skywalking.sh gateway   # 启动 gateway
#   ./start-with-skywalking.sh auth      # 启动 auth
#   ./start-with-skywalking.sh customer  # 启动 customer
#   ./start-with-skywalking.sh all       # 同时启动 gateway+auth+customer
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SW_AGENT_HOME="${SKYWALKING_AGENT_HOME:-$SCRIPT_DIR/skywalking-agent}"
SW_AGENT_JAR="$SW_AGENT_HOME/skywalking-agent.jar"
SW_BACKEND="${SW_BACKEND:-localhost:11800}"

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 检查 SkyWalking Agent
check_agent() {
    if [ -f "$SW_AGENT_JAR" ]; then
        echo -e "${GREEN}[INFO] SkyWalking Agent: $SW_AGENT_JAR${NC}"
        return 0
    else
        echo -e "${YELLOW}[WARN] SkyWalking Agent 未找到: $SW_AGENT_JAR${NC}"
        echo -e "${YELLOW}[WARN] 将以无追踪模式启动${NC}"
        return 1
    fi
}

# 启动单个服务
start_service() {
    local module=$1
    local service_name=$2
    local port=$3

    echo -e "${GREEN}[INFO] 启动 $service_name (:$port)...${NC}"
    cd "$PROJECT_DIR/$module"

    local JVM_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/heapdump.hprof -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k -XX:+DisableExplicitGC -Dfile.encoding=UTF-8"

    if check_agent; then
        JVM_OPTS="-javaagent:$SW_AGENT_JAR -Dskywalking.agent.service_name=$service_name -Dskywalking.collector.backend_service=$SW_BACKEND $JVM_OPTS"
    fi

    nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="$JVM_OPTS" \
        > "$SCRIPT_DIR/logs/${service_name}.log" 2>&1 &

    echo -e "${GREEN}[INFO] $service_name PID=$! 日志: scripts/logs/${service_name}.log${NC}"
}

# 确保日志目录存在
mkdir -p "$SCRIPT_DIR/logs"

case "${1:-}" in
    gateway)
        start_service "yyzx-gateway" "yyzx-gateway" "8080"
        ;;
    auth)
        start_service "yyzx-auth" "yyzx-auth" "8081"
        ;;
    customer)
        start_service "yyzx-customer" "yyzx-customer" "8082"
        ;;
    all)
        start_service "yyzx-gateway" "yyzx-gateway" "8080"
        sleep 5
        start_service "yyzx-auth" "yyzx-auth" "8081"
        sleep 3
        start_service "yyzx-customer" "yyzx-customer" "8082"
        ;;
    *)
        echo "用法: $0 {gateway|auth|customer|all}"
        echo ""
        echo "环境变量:"
        echo "  SKYWALKING_AGENT_HOME  SkyWalking Agent 目录（默认: ./skywalking-agent）"
        echo "  SW_BACKEND             OAP 后端地址（默认: localhost:11800）"
        exit 1
        ;;
esac

echo -e "${GREEN}[INFO] 启动完成。查看日志: tail -f $SCRIPT_DIR/logs/*.log${NC}"
