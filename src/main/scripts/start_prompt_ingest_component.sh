#!/bin/sh

SCRIPT_DIR=$(dirname "$0")

java -classpath "$SCRIPT_DIR/../lib/*" dk.statsbiblioteket.newspaper.promptdomsingester.component.PromptDomsIngesterComponent $SCRIPT_DIR/../conf/config.properties
