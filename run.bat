@echo off
echo Starting Student Management Learning System...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Apache Maven
    pause
    exit /b 1
)

echo Checking dependencies...
mvn dependency:resolve -q

echo.
echo Starting application...
mvn javafx:run

pause