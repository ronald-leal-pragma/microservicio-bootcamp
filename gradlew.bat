@echo off
REM Minimal Gradle Wrapper BAT: expects gradle-wrapper.jar in gradle\wrapper\
setlocal
set DIRNAME=%~dp0
if exist "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
	"%JAVA_HOME%\bin\java.exe" -cp "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
	echo gradle-wrapper.jar not found. Falling back to system gradle if available.
	gradle %*
)
endlocal
