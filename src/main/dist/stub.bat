@ 2>/dev/null # 2>nul & echo off & goto BOF

:BOF
@echo off
start %JAVA_HOME%\bin\java %JAVA_OPTS% -cp "%~dp0/libs/*;%~dp0/libs/" -jar "%~dpnx0" %*
exit /B %errorlevel%
