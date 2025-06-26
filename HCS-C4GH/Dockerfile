FROM eclipse-temurin:17-jdk

ENV GLASSFISH_VERSION=6.2.5 \
    GLASSFISH_HOME=/opt/glassfish \
    DEPLOY_DIR=/opt/glassfish/glassfish/domains/domain1/autodeploy

# Instal·lació de dependències
RUN apt-get update && \
    apt-get install -y curl unzip python3 python3-pip python3-dev gcc libffi-dev && \
    pip3 install --upgrade setuptools --break-system-packages && \
    pip3 install crypt4gh --break-system-packages && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


# Descarregar i instal·lar GlassFish 6.2.5
RUN curl -L -o glassfish.zip https://download.eclipse.org/ee4j/glassfish/glassfish-${GLASSFISH_VERSION}.zip && \
    unzip glassfish.zip -d /opt && \
    mv /opt/glassfish* ${GLASSFISH_HOME} && \
    rm glassfish.zip

WORKDIR ${GLASSFISH_HOME}

# Copiar el WAR
COPY target/HCSC4GH-1.war ${DEPLOY_DIR}/

EXPOSE 8080 4848

CMD ["bash", "-c", "/opt/glassfish/bin/asadmin start-domain --verbose"]
