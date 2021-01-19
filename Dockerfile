FROM ghcr.io/alphagov/verify/gradle:gradle-jdk11 as build

WORKDIR /test-rp
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
# There is an issue running idea.gradle in the container
# So just make this an empty file
RUN touch idea.gradle
RUN gradle install

COPY src/ src/

RUN gradle installDist

ENTRYPOINT ["gradle", "--no-daemon"]
CMD ["tasks"]

FROM ghcr.io/alphagov/verify/java:openjdk-11

WORKDIR /test-rp

COPY configuration/local/test-rp.yml test-rp.yml
COPY --from=build /test-rp/build/install/test-rp .

CMD bin/test-rp server test-rp.yml
