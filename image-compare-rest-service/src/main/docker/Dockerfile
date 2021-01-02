FROM  adoptopenjdk/openjdk11

RUN apt-get update && apt-get install ghostscript imagemagick -y

ADD ../fixpolicy.sh /
RUN chmod +x /fixpolicy.sh && /fixpolicy.sh && rm /fixpolicy.sh

RUN apt-get clean && rm -rf /var/lib/apt/lists/*

#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring:spring
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","de.brakhageit.de.brakhageit.imagecompareservice.de.brakhageit.imagecompareservice.rest.ImageCompareServiceApplication"]