#!/bin/bash

source __environment.sh

cd $SRC

echo $COMPILER -classpath $COMP_CLASSP $COMP_FLAGS -d $TARGET $SOURCES
#$COMPILER -classpath $COMP_CLASSP $COMP_FLAGS -encoding ISO-8859-1 -Xlint:unchecked -d $TARGET $SOURCES
$COMPILER -classpath $COMP_CLASSP $COMP_FLAGS -encoding ISO-8859-1 -d $TARGET $SOURCES

cd $BASE

