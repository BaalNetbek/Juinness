#!/bin/bash

# *************************
# note that these are relative not absolute paths because
# your paths probably differs from mine
# *************************
export BASE=..
export LIB=$BASE/lib
export SRC=$BASE/src
export TARGET=$BASE/bin
export BIN=$BASE/bin
export API_PATH=$BASE/api
export SCRIPT=./scriptsLinux

PACKAGE_JUINNESS=./juinness/*.java
PACKAGE_M3G=./juinness/m3g/*.java
PACKAGE_UTIL=./juinness/util/*.java
PACKAGE_FAKE=./javax/microedition/m3g/FakeTexture2D.java
PACKAGE_GEN=./juinness/CodeGenerator.java

export SOURCES="$PACKAGE_FAKE $PACKAGE_JUINNESS $PACKAGE_M3G $PACKAGE_UTIL"

# *************************
# jm3d stuff
# *************************
export MOBILE=$LIB/classes.zip

# *************************
# j3d stuff
# *************************
export J3D=$LIB/j3daudio.jar:$LIB/j3dcore.jar:$LIB/j3dutils.jar:$LIB/vecmath.jar
# *************************
# loader stuff e.g. CHANGE THE content of the ..models/loader.xml
# *************************
 export LOADER=$LIB/cv97r140.jar

# *************************
# The final 1) compile time and 2) run time CLASSPATH
# Note that the name CLASSP is used so that we do not mess 
# e.g. with some other CLASSPATH
# *************************
export COMP_CLASSP=.:$MOBILE:$J3D:$LOADER
export RT_CLASSP=.:$MOBILE:$J3D:$LOADER

export COMPILER=javac 

# *************************
# Set some other flags to java and compiler(javac/jikes) if needed
# *************************
export COMP_FLAGS=-Xlint:deprecation 
export RT_FLAGS=-Djava.library.path=$PATH:$BASE/lib

# *************************
# Should be enough just to modify the following parameters
# *************************
export WTK=/opt/java/WTK2.2
export PATH=$PATH$:../lib:$WTK/bin
export WTK_APPS=$WTK/apps
export WTK_LIB=$WTK/lib
export ARG1=../models/apina.wrl
export ARG2=0
export ARG3=$WTK_APPS/Demoni/res/test.m3g
export ARG4=../models/background.jpg
export ARG5=../models/loader.xml
