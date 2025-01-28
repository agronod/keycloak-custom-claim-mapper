# Agronod Keycloak Custom Claim Mapper

Requires 
    - OpenJDK 21 or higher
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
- In realm settings set Required SSL to "none"

- Create a client with ID: "myclient", "Root URL": "https://www.keycloak.org/app/" and "Valid redirect URIs": "https://www.keycloak.org/app/*" and "Web origins": *
- Go to 👉 https://www.keycloak.org/app/ 👈 Click "Save" then "Sign in". You should see your login page

Alternativly running on local App:
- set "Root URL": "http://localhost:3000" and "Valid redirect URIs": "http://localhost:3000/*"  and "Web origins": *
- Go to http://localhost:3000

- Create client scope "agro-id"
- Add Mapper (Configure new mapper) in scope "agro-id"
- Select "Agronod Custom Claim Mapper" and name it "agronodclaimmapper"
- In "myclient" add client scope "agro-id" as default


To run shell in keycloak container.
```
docker exec -it keycloak-testing-container sh
```
