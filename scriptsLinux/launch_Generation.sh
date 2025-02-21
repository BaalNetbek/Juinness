#!/bin/bash

source __environment.sh

rm $SRC/juinness/GeneratedTraverser.java 2>/dev/null
rm $SRC/juinness/GeneratedTranslator.java 2>/dev/null

cd $SRC

echo $COMPILER -classpath $COMP_CLASSP $COMP_FLAGS -encoding ISO-8859-1 -d $TARGET $PACKAGE_GEN
$COMPILER -classpath $COMP_CLASSP $COMP_FLAGS -encoding ISO-8859-1 -d $TARGET $PACKAGE_GEN

cd $BASE
cd $SCRIPT

export JAVA_FILE=juinness.CodeGenerator
export ARG2=0
source __runner.sh
cd $SCRIPT
