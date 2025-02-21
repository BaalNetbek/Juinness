@echo off
REM *************************
REM lines beginning with REM are comments
REM @echo off disables echoing
REM @echo on enables echoing
REM SET defines a variable
REM *************************

REM *************************
REM note that these are relative not absolute paths because
REM your paths probably differs from mine
REM *************************
SET BASE=..
SET LIB=%BASE%\lib
SET SRC=.\src
SET TARGET=%BASE%\bin
SET BIN=.\bin
SET API_PATH=..\api

SET PACKAGE_JUINNESS=.\juinness\*.java
SET PACKAGE_ABSYN=.\juinness\absyn\*.java
SET PACKAGE_M3G=.\juinness\m3g\*.java
SET PACKAGE_UTIL=.\juinness\util\*.java
SET PACKAGE_FAKE=.\javax\microedition\m3g\FakeTexture2D.java

SET SOURCES=%PACKAGE_FAKE% %PACKAGE_JUINNESS% %PACKAGE_ABSYN% %PACKAGE_M3G% %PACKAGE_UTIL% 

REM *************************
REM jm3d stuff
REM *************************
SET MOBILE=%LIB%\classes.zip

REM *************************
REM j3d stuff
REM *************************
SET J3D=%LIB%\j3daudio.jar;%LIB%\j3dcore.jar;%LIB%\j3dutils.jar;%LIB%\vecmath.jar
REM *************************
REM loader stuff e.g. CHANGE THE content of the ..models\loader.xml
REM *************************
REM SET LOADER=%LIB%\someLoader.jar%LIB%\someOtherLoader.jar

REM *************************
REM The final 1) compile time and 2) run time CLASSPATH
REM Note that the name CLASSP is used so that we do not mess 
REM e.g. with some other CLASSPATH
REM *************************
SET COMP_CLASSP=.;%MOBILE%;%J3D%;%LOADER%
SET RT_CLASSP=.;%MOBILE%;%J3D%;%LOADER%

SET COMPILER=javac 

REM *************************
REM Set some other flags to java and compiler(javac/jikes) if needed
REM *************************
SET COMP_FLAGS=-Xlint:deprecation
SET RT_FLAGS=

REM *************************
REM Should be enough just to modify the following parameters
REM *************************
SET WTK=C:\Java\WTK22
SET PATH=%PATH%;..\lib;%WTK%\bin
SET WTK_APPS=%WTK%\apps
SET WTK_LIB=%WTK%\lib
SET RT_CLASSP=%RT_CLASSP%;%WTK_LIB%/

SET ARG1=../models/apina.wrl
SET ARG2=0
SET ARG3=%WTK_APPS%\Demoni\res\test.m3g
SET ARG4=../models/background.jpg
SET ARG5=../models/loader.xml

