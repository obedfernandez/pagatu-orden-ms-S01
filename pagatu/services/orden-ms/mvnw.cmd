@echo off
setlocal

set MAVEN_VERSION=3.9.9

if "%USERPROFILE%"=="" set USERPROFILE=%HOMEDRIVE%%HOMEPATH%

set MAVEN_BASE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin
set MAVEN_HOME=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Descargando Apache Maven %MAVEN_VERSION% para Maven Wrapper...

    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $zip='%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip'; $dest='%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile $zip; New-Item -ItemType Directory -Force -Path $dest | Out-Null; Expand-Archive -Force $zip $dest; Remove-Item $zip"

    if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*