@echo off
setlocal enabledelayedexpansion
set AGENT_DIR=%~dp0skywalking-agent
set AGENT_JAR=%AGENT_DIR%\skywalking-agent.jar

if exist "%AGENT_JAR%" (
    set AGENT_OPTS=-javaagent:%AGENT_JAR% -Dskywalking.agent.service_name=yyzx-customer -Dskywalking.collector.backend_service=localhost:11800
) else (
    set AGENT_OPTS=
)

cd /d "%~dp0..\yyzx-customer"
echo [INFO] Starting yyzx-customer :8082
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="!AGENT_OPTS! -Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/heapdump.hprof -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k -XX:+DisableExplicitGC -Dfile.encoding=UTF-8"
endlocal
