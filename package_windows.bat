@echo off
echo ===================================================
echo Packaging Smart Complaint Registration Portal (Windows)
echo ===================================================

echo 1. Cleaning and compiling...
if exist bin rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/smartcomplaint/model/*.java src/com/smartcomplaint/util/*.java src/com/smartcomplaint/dao/*.java src/com/smartcomplaint/service/*.java src/com/smartcomplaint/ui/*.java src/com/smartcomplaint/ui/components/*.java src/com/smartcomplaint/ui/views/*.java src/com/smartcomplaint/*.java

echo 2. Preparing dist directory...
if exist dist rmdir /s /q dist
mkdir dist
mkdir dist\lib
xcopy /s /e /q /y lib\* dist\lib\

echo 3. Generating Manifest...
echo Manifest-Version: 1.0 > manifest.txt
echo Main-Class: com.smartcomplaint.MainApp >> manifest.txt
echo Class-Path: lib/flatlaf-3.5.2.jar lib/mysql-connector-j-8.4.0.jar lib/slf4j-api-2.0.12.jar lib/slf4j-simple-2.0.12.jar lib/sqlite-jdbc-3.45.2.0.jar >> manifest.txt

echo 4. Creating Executable JAR...
jar cvfm dist/SmartComplaintPortal.jar manifest.txt -C bin .

echo 5. Running jpackage (This may take a minute)...
jpackage --input dist --name SmartComplaintPortal --main-jar SmartComplaintPortal.jar --type app-image

echo ===================================================
echo Packaging Complete! Check the root folder for the 'SmartComplaintPortal' folder.
echo You can zip this folder to distribute the software!
echo ===================================================
