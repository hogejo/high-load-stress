FROM amazoncorretto:21-alpine

ARG PROJECT_NAME
ARG PROJECT_VERSION

COPY --chown=nobody:nobody build/distributions/flat /app

USER nobody
WORKDIR /app

ENV JAR_NAME="${PROJECT_NAME}-${PROJECT_VERSION}.jar"
ENTRYPOINT [ "sh", "-c", "java -jar ${JAR_NAME} $0 $@" ]
CMD [ "--help" ]
