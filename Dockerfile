ARG registry_image_gradle=gradle:6.7.0-jdk11
ARG registry_image_jdk=openjdk:11.0.11-jre

FROM ${registry_image_gradle} as build

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

FROM ${registry_image_jdk}

WORKDIR /test-rp

COPY configuration/local/test-rp.yml test-rp.yml
COPY --from=build /test-rp/build/install/test-rp .

CMD bin/test-rp server test-rp.yml
