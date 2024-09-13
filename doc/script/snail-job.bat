@echo off
REM snail-job.bat start|stop|restart|status
set AppName=snail-job-server-exec.jar

REM JVM options
set JVM_OPTS=-Dname=%AppName% -Duser.timezone=Asia/Shanghai -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC
set APP_HOME=%cd%

REM Check if an action is passed, default to "start" if empty
if "%1"=="" (
    echo No action provided, default to: start
    set ACTION=start
) else (
    set ACTION=%1
)

if "%AppName%"=="" (
    echo No application name provided
    exit /b 1
)

REM Check the action before executing
if /i "%ACTION%"=="start" (
    call :Start
) else if /i "%ACTION%"=="stop" (
    call :Stop
) else if /i "%ACTION%"=="restart" (
    call :Restart
) else if /i "%ACTION%"=="status" (
    call :Status
) else (
    echo Invalid action. Valid actions are: {start|stop|restart|status}
    exit /b 1
)

goto :eof

:Start
REM Check if the program is already running using jps
for /f "tokens=1" %%i in ('jps -l ^| findstr %AppName%') do set PID=%%i

if defined PID (
    echo %AppName% is already running with PID %PID%...
) else (
    start "" /b javaw %JVM_OPTS% -jar %AppName%
    echo Start %AppName% success...
)
goto :eof

:Stop
set "PID="

REM Find the process using jps and stop it
for /f "tokens=1" %%i in ('jps -l ^| findstr %AppName%') do (
    set "PID=%%i"
)

REM Removing any extra spaces from PID (just in case)
for /f "tokens=* delims= " %%i in ("%PID%") do set "PID=%%i"

REM Verify if PID is defined
if defined PID (
    REM Using TASKKILL to kill the process
    taskkill /PID %PID% /T /F

    REM Check if taskkill was successful
    if %errorlevel% NEQ 0 (
        echo Failed to stop %AppName% with PID %PID%. Error level: %errorlevel%
    ) else (
        echo %AppName% with PID %PID% has been stopped.
    )
) else (
    echo %AppName% is already stopped or not running.
)
goto :eof


:Restart
call :Stop
timeout /t 2 >nul
call :Start
goto :eof

:Status
REM Check if the program is running using jps
for /f "tokens=1" %%i in ('jps -l ^| findstr %AppName%') do set PID=%%i

if defined PID (
    echo %AppName% is running with PID %PID%...
) else (
    echo %AppName% is not running...
)
goto :eof
