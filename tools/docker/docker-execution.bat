@echo off
setlocal enabledelayedexpansion

:: Configuration
set JAR_PATH=target\padelcenter-*.jar
set DOCKER_DIR=tools\docker
set DOCKERFILE_NAME=Dockerfile
set IMAGE_NAME=padelcenter
set FINAL_JAR_NAME=%IMAGE_NAME%.jar
set FINAL_YML_NAME=application-local.yaml
set YML_PATH=src\main\resources\application-local.yaml

echo Moving to project root...
cd /d "%~dp0..\.."
if errorlevel 1 (
 echo Failed to change to project root.
 exit /b 1
)
echo Current directory: %cd%

echo Running Maven clean install...
call mvn clean install -B -DskipTests
if errorlevel 1 (
 echo Maven build failed.
 exit /b 1
)

echo Checking if JAR file exists...
for %%f in (%JAR_PATH%) do (
 set "JAR_FILE=%%f"
)
if not defined JAR_FILE (
 echo No JAR found at path: %JAR_PATH%
 exit /b 1
)

echo Copying JAR to %DOCKER_DIR%\%FINAL_JAR_NAME%...
copy /Y "!JAR_FILE!" "%DOCKER_DIR%\%FINAL_JAR_NAME%"
if errorlevel 1 (
 echo Failed to copy JAR file.
 exit /b 1
)

echo Checking if application local file exists...
for %%f in (%YML_PATH%) do (
 set "YML_FILE=%%f"
)
if not defined YML_FILE (
 echo No application local found at path: %YML_PATH%
 exit /b 1
)

echo Copying application-local to %DOCKER_DIR%\%FINAL_YML_NAME%...
copy /Y "!YML_FILE!" "%DOCKER_DIR%\%FINAL_YML_NAME%"
if errorlevel 1 (
 echo Failed to copy application-local file.
 exit /b 1
)

echo Changing to Docker directory...
cd /d "%DOCKER_DIR%"
if errorlevel 1 (
 echo Failed to change to Docker directory.
 exit /b 1
)
echo Current directory: %cd%

echo Building image...
call podman build -t padelcenter:local . --no-cache
if errorlevel 1 (
 echo podman build failed.
 exit /b 1
)

echo Launching podman-compose down...
call podman compose down
if errorlevel 1 (
 echo podman compose failed.
 exit /b 1
)

echo Launching podman-compose...
call podman compose up -d
if errorlevel 1 (
 echo podman compose failed.
 exit /b 1
)

echo Cleaning up copied JAR...
del "%FINAL_JAR_NAME%"
if errorlevel 1 (
 echo Failed to delete JAR file.
 exit /b 1
)

echo Cleaning up copied application local...
del "%FINAL_YML_NAME%"
if errorlevel 1 (
 echo Failed to delete application local file.
 exit /b 1
)

echo Script completed successfully.