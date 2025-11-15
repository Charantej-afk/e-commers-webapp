FROM tomcat:9.0

# Copy WAR
COPY ecommerce-app.war /usr/local/tomcat/webapps/

# Expose port 8080
EXPOSE 8080

# Set CATALINA_OPTS to bind to all interfaces
ENV CATALINA_OPTS="-Djava.rmi.server.hostname=0.0.0.0 -Djava.net.preferIPv4Stack=true"

# Start Tomcat in foreground
CMD ["catalina.sh", "run"]
