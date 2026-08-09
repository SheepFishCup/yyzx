@echo off
setlocal enabledelayedexpansion

set AGENT_DIR=%~dp0skywalking-agent
set AGENT_JAR=%AGENT_DIR%\skywalking-agent.jar

if exist "%AGENT_JAR%" (
    echo [INFO] SkyWalking Agent found, starting with tracing...
    set AGENT_OPTS=-javaagent:%AGENT_JAR% -Dskywalking.agent.service_name=yyzx-gateway -Dskywalking.collector.backend_service=localhost:11800
) else (
    echo [WARN] SkyWalking Agent not found at %AGENT_JAR%
    echo [INFO] Starting without tracing...
    set AGENT_OPTS=
)

cd /d "%~dp0..\yyzx-gateway"
echo [INFO] Starting yyzx-gateway :8080
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="!AGENT_OPTS! -Xms256m -Xmx512m"
endlocal
