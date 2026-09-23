@echo off
echo ===================================================
echo Compiling Smart Complaint Registration Portal...
echo ===================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin src/com/smartcomplaint/model/*.java src/com/smartcomplaint/util/*.java src/com/smartcomplaint/dao/*.java src/com/smartcomplaint/service/*.java src/com/smartcomplaint/ui/*.java src/com/smartcomplaint/ui/components/*.java src/com/smartcomplaint/ui/views/*.java src/com/smartcomplaint/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished with 0 errors.
) else (
    echo [ERROR] Compilation failed!
)
