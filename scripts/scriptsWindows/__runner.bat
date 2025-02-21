@echo off
REM *************************
REM Runs all the java files
REM *************************

cd %BIN%
@echo on
java -classpath %RT_CLASSP% %RT_FLAGS% %JAVA_FILE% %ARG1% %ARG2% %ARG3% %ARG4%  %ARG5%
@echo off
cd %BASE%

@echo on
pause
