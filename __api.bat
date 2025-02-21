call __environment.bat

cd %SRC%

javadoc -d %API_PATH% -classpath %COMP_CLASSP% -private -author -source %SOURCES%

cd %BASE%

pause
