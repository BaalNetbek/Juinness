@echo off
REM *************************
REM Generates the java files
REM *************************
call __environment.bat

cd %SRC%
@echo on

%COMPILER% -classpath %COMP_CLASSP% %COMP_FLAGS% -d %TARGET% %PACKAGE_GEN%

@echo off
cd %BASE%

@echo on

SET JAVA_FILE=juinness.CodeGenerator
SET ARG2=0
call __runner.bat
