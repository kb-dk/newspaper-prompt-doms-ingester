#!/bin/sh

SCRIPT_DIR=$(dirname $(readlink -f $0))

java java -classpath "$SCRIPT_DIR/../conf:$SCRIPT_DIR/../lib/*" \
 dk.statsbiblioteket.newspaper.promptdomsingester.component.PromptDomsIngesterComponent \
 $SCRIPT_DIR/../conf/config.properties
