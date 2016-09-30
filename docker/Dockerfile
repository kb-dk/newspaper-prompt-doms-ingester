FROM openjdk:7-jre

ENV component="newspaper-prompt-doms-ingester-1.9-SNAPSHOT"

WORKDIR /opt/

#Copy component to docker container and extract
ADD ${component}-package.tar.gz .

COPY config.properties logback.xml /conf/

WORKDIR /opt/${component}

ENTRYPOINT [ "java", "-classpath", "/conf:lib/*", "dk.statsbiblioteket.newspaper.promptdomsingester.component.PromptDomsIngesterComponent", "/conf/config.properties"]


