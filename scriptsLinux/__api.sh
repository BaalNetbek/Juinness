#!/bin/bash

source __environment.sh

cd $SRC

javadoc -d $API_PATH -classpath $COMP_CLASSP -private -author -source $SOURCES

cd $BASE
