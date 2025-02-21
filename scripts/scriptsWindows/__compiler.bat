@echo off
REM *************************
REM Compiles the java files
REM *************************
call __environment.bat

cd %SRC%
@echo on

%COMPILER% -classpath %COMP_CLASSP% %COMP_FLAGS% -d %TARGET% %SOURCES%

@echo off
cd %BASE%

@echo on
pause
