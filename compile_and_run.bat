@echo off
:start
cls
echo Compiling Matisense Application...

REM Create output directory
if not exist "out" mkdir out

REM Compile all Java files
echo Compiling source files...
javac -cp ".;lib\mysql-connector-j-8.0.33.jar" -d out ^
    src\com\matisense\*.java ^
    src\com\matisense\config\*.java ^
    src\com\matisense\dao\*.java ^
    src\com\matisense\exception\*.java ^
    src\com\matisense\model\*.java ^
    src\com\matisense\service\*.java ^
    src\com\matisense\ui\*.java ^
    src\com\matisense\util\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting application...
echo.
echo Default admin login: username=admin, password=adminadmin123
echo.
java -cp "out;lib\mysql-connector-j-8.0.33.jar" com.matisense.MainApplication

echo.
echo Application exited. Press Ctrl+C to stop auto-restart.
timeout /t 3 >nul
goto start
