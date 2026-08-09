@echo off
setlocal enabledelayedexpansion
set AGENT_DIR=%~dp0skywalking-agent
set AGENT_JAR=%AGENT_DIR%\skywalking-agent.jar
if exist "%AGENT_JAR%" (set AGENT_OPTS=-javaagent:%AGENT_JAR% -Dskywalking.agent.service_name=yyzx-notification -Dskywalking.collector.backend_service=localhost:11800) else (set AGENT_OPTS=)
cd /d "%~dp0..\yyzx-notification"
echo [INFO] Starting yyzx-notification
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="!AGENT_OPTS! -Xms256m -Xmx512m"
endlocal
