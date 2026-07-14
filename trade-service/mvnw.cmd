@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat" %*
if exist "%USERPROFILE%\mavenrc_pre.cmd" call "%USERPROFILE%\mavenrc_pre.cmd" %*
:skipRcPre

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

@REM ==== END VALIDATION ====

:init

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" goto endReadAdditionalConfig

@setlocal EnableExtensions EnableDelayedExpansion
for /F "usebackq delims=" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") do set JVM_CONFIG_MAVEN_PROPS=!JVM_CONFIG_MAVEN_PROPS! %%a
@endlocal & set JVM_CONFIG_MAVEN_PROPS=%JVM_CONFIG_MAVEN_PROPS%

:endReadAdditionalConfig

SET MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set DOWNLOAD_URL="https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"

for /f "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

if not "%MAVEN_SKIP_WRAPPER_DOWNLOAD%" == "true" (
    IF NOT EXIST "%WRAPPER_JAR%" (
        echo Downloading Maven wrapper: %DOWNLOAD_URL%
        powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%' -UseBasicParsing}"
    )
    if exist "%WRAPPER_JAR%" goto downloadMavenDist
    echo ERROR: Maven wrapper jar not found. >&2
    goto error
)

:downloadMavenDist
SET MAVEN_DIST_URL=@distUrl@
for /f "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="distributionUrl" SET MAVEN_DIST_URL=%%B
)
echo Downloading Maven distribution from: %MAVEN_DIST_URL%
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $distUrl='%MAVEN_DIST_URL%'; $distName=$distUrl.Substring($distUrl.LastIndexOf('/')+1); $dest='%USERPROFILE%\.m2\wrapper\dists\' + $distName; if (!(Test-Path $dest)) { New-Item -ItemType Directory -Force -Path (Split-Path $dest) | Out-Null; Invoke-WebRequest -Uri $distUrl -OutFile $dest -UseBasicParsing; Write-Host 'Maven distribution downloaded to: ' $dest; Expand-Archive -Path $dest -DestinationPath (Split-Path $dest) -Force } }"
SET MAVEN_HOME_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
if exist "%MAVEN_HOME_DIR%\bin\mvn.cmd" goto runMaven
for /f "delims=" %%D IN ("%USERPROFILE%\.m2\wrapper\dists") DO (
    IF EXIST "%%~fD\apache-maven-3.9.6\bin\mvn.cmd" SET MAVEN_HOME_DIR=%%~fD\apache-maven-3.9.6
    IF EXIST "%%~fD\apache-maven-3.9.6\bin\mvn.cmd" goto runMaven
)
echo Checking for extracted Maven in %USERPROFILE%\.m2\wrapper\dists
dir "%USERPROFILE%\.m2\wrapper\dists" /s /b 2>nul | findstr "mvn.cmd"
if %ERRORLEVEL% == 0 goto runMaven
echo Maven distribution not found. Please download Maven manually. >&2
goto error

:runMaven
SET MAVEN_HOME=%MAVEN_HOME_DIR%
SET MAVEN_CMD="%MAVEN_HOME%\bin\mvn.cmd"
echo Using Maven at: %MAVEN_HOME%
%MAVEN_CMD% %*

:end
@REM End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:error
set ERROR_CODE=1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
