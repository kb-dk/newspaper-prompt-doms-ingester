#!/usr/bin/env bash

rm -f /opt/${artifactId}/conf

# If we have something in the /conf folder, use that
if [ -f /conf/config.properties ]; then
    ln -s /conf /opt/${artifactId}/conf
else # Else, parse the conf.orig folder with the environment
    if [ ! -f /conf/config.properties ]; then
        mkdir -p /opt/${artifactId}/conf/
        for file in /opt/${artifactId}/conf.orig/*.properties; do
            outputFile="/opt/${artifactId}/conf/$(basename $file)"
            cat $file | while read line; do
                #This oneliner converts lines like
                #    autonomous.lockserver.url={zookeeper.host}:{zookeper.port}
                #To
                #    autonomous.lockserver.url=${zookeeper_host}:${zookeper_port}
                parsedLine=$(echo $line | sed 's/\(=[^{]*\){/\1${/g' | sed 's/\(}[^{]*\){/\1${/g' | sed 's/\({[^.}]*\)\./\1_/g' | sed 's/\({[^.}]*\)\./\1_/g')
                # Use eval to substitute the env variables into the config line
                eval "echo \"$parsedLine\" >> ${outputFile}"
            done
        done
        shopt -s extglob  # to enable extglob
        cp /opt/${artifactId}/conf.orig/!\(*.properties\) /opt/${artifactId}/conf/
        shopt -u extglob
    fi
fi

#When the config is done, just start whatever command is given
exec bash -c "$@"
