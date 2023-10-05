# Agronod Keycloak Custom Claim Mapper

Requires 
    - OpenJDK 17 or higher
    - maven

## build

mvn clean package

## deploy

Copy the jar file "custom_claim_mapper-1.0.jar" to Keycloaks providers directory
/opt/keycloak/providers/


## test on local docker

Change path in file start_keycloak_testing_container.sh to point to your custom_claim_mapper-1.0.jar file.

Then run
```
./start_keycloak_testing_container.sh
```

Once your container is up and running:
- Log into the admin console 👉 http://localhost:8080/admin username: admin, password: admin 👈
- Create a realm named "myrealm"
- Create a client with ID: "myclient", "Root URL": "https://www.keycloak.org/app/" and "Valid redirect URIs": "https://www.keycloak.org/app/*"
- Select Login Theme: agronod-b2b-theme (don't forget to save at the bottom of the page)
- Go to 👉 https://www.keycloak.org/app/ 👈 Click "Save" then "Sign in". You should see your login page
- The mapper should be in Scopes.


To run shell in keycloak container.
```
docker exec -it keycloak-testing-container sh
```
