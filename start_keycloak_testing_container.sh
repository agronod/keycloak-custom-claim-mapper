#!/bin/bash

docker rm keycloak-testing-container || true

docker run \
   -p 8080:8080 \
   --name keycloak-testing-container \
   -e KEYCLOAK_ADMIN=admin \
   -e KEYCLOAK_ADMIN_PASSWORD=admin \
   -e JAVA_OPTS=-Dkeycloak.profile=preview \
   -v /home/david/Git/Agronod/keycloak-custom-claim-mapper/target/custom_claim_mapper-1.0.jar:/opt/keycloak/providers/custom_claim_mapper-1.0.jar:rw \
   -it quay.io/keycloak/keycloak:19.0.1 \
   start-dev
