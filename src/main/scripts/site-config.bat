@echo off
rem PingAm Site Configuration Tool
rem Windows Batch Script

rem Set the current directory as the base directory
set BASE_DIR=%~dp0

rem Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed or not in the PATH. Please install Java 11 or higher.
    exit /b 1
)

rem Execute the Java application with all arguments passed to this script
java -jar "%BASE_DIR%lib\${project.build.finalName}.jar" %*

exit /b %ERRORLEVEL%
