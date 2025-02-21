#!/bin/bash

cd $BIN
echo java -classpath $RT_CLASSP $RT_FLAGS $JAVA_FILE $ARG1 $ARG2 $ARG3 $ARG4 $ARG5
java -classpath $RT_CLASSP $RT_FLAGS $JAVA_FILE $ARG1 $ARG2 $ARG3 $ARG4 $ARG5

cd $BASE
