# Imatge base amb Java 17
FROM eclipse-temurin:17-jdk

# Variables
ENV GLASSFISH_VERSION=6.2.5 \
    GLASSFISH_HOME=/opt/glassfish \
    DEPLOY_DIR=/opt/glassfish/glassfish/domains/domain1/autodeploy

# Instal·lació de paquets bàsics
RUN apt-get update && apt-get install -y curl unzip && \
    rm -rf /var/lib/apt/lists/*

# Descarregar i instal·lar GlassFish
RUN curl -L -o glassfish.zip https://download.eclipse.org/ee4j/glassfish/glassfish-${GLASSFISH_VERSION}.zip && \
    unzip glassfish.zip -d /opt && \
    mv /opt/glassfish* ${GLASSFISH_HOME} && \
    rm glassfish.zip


# Crear carpeta de treball
WORKDIR ${GLASSFISH_HOME}

# Copiar el .war al directori d'autodeploy
COPY target/am-1.war ${DEPLOY_DIR}/

# Expose ports
EXPOSE 8080 4848

# Comanda per iniciar el servidor
CMD ["bash", "-c", "/opt/glassfish/bin/asadmin start-domain --verbose"]