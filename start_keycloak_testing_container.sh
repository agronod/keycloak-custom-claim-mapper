#!/bin/bash

docker rm keycloak-testing-container || true

docker run \
   -p 8080:8080 \
   --name keycloak-testing-container \
   -e KEYCLOAK_ADMIN=admin \
   -e KEYCLOAK_ADMIN_PASSWORD=admin \
   -e JAVA_OPTS=-Dkeycloak.profile=preview \
   -v /home/david/Git/Agronod/keycloak-custom-claim-mapper/target/keycloak-custom-claim-mapper-jar-with-dependencies.jar:/opt/keycloak/providers/keycloak-custom-claim-mapper-jar-with-dependencies.jar:rw \
   -it quay.io/keycloak/keycloak:18.0.0 \
   start-dev
