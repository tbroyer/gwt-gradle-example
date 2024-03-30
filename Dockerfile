FROM eclipse-temurin:17-jre

# FIXME: follow Dockerfile best practices (e.g. create a non-root user, use an entrypoint, etc.)

# XXX: this could be improved by separating third-party JARs into one layer, and project
# classes and resources into another layer; this is out of scope for this example.
COPY server/build/install/server /opt/example/
COPY client/build/gwtc/war /opt/example/www

EXPOSE 8000

CMD [ "java", "-cp", "/opt/example/lib/*", "gwt.example.server.Main", "/opt/example/www" ]
