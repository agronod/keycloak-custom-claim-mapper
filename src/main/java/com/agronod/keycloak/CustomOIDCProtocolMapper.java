package com.agronod.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;

public class CustomOIDCProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-customprotocolmapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static Logger logger = Logger.getLogger(CustomOIDCProtocolMapper.class);
    private DatabaseAccess databaseAccess;

    // To configure this in Terraform.
    // https://registry.terraform.io/providers/mrparkers/keycloak/latest/docs/resources/generic_protocol_mapper
    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("connectionstring");
        property.setLabel("Database connectionstring");
        property.setHelpText("Connectionstring to database");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    public CustomOIDCProtocolMapper() {
        this.databaseAccess = new DatabaseAccess();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Agronod Custom Claim Mapper";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Adds claims from Agronods User/claims API";
    }

    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
            KeycloakSession keycloakSession,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        try {
            String userId = token.getSubject();
            Boolean isEmailVerified = token.getEmailVerified();
            String currentScope = token.getScope();

            Connection conn = this.databaseAccess
                    .createDatabaseConnection(mappingModel.getConfig().get("connectionstring"));

            List<AgronodKonton> konton = this.databaseAccess.fetchOwnAgroKontoWithAffarspartners(userId, conn);

            logger.info("Fetched own affarspartners");

            UserInfo userInfo = this.databaseAccess.fetchUserInfo(userId, conn);
            logger.info("Fetched user Info");

            // Admin roles
            konton = this.databaseAccess.fetchAdminRoles(userId, conn, konton);

            logger.info("Fetched admin roles from other");

            String jsonKonton = "";
            ObjectMapper mapper = new ObjectMapper();

            jsonKonton = mapper.writeValueAsString(konton);
            token.getOtherClaims().put("agronodKonton", jsonKonton);

            if (userInfo.email != null && userInfo.email.length() > 0) {
                token.getOtherClaims().put("email", userInfo.email);
            }

            if (userInfo.name != null && userInfo.name.length() > 0) {
                token.getOtherClaims().put("name", userInfo.name);
            }

            if (currentScope.contains("ssn") && userInfo.ssn != null && userInfo.ssn.length() > 0) {
                token.getOtherClaims().put("ssn", userInfo.ssn);
            }

            setClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);
            logger.info("Set updated claims for user");
        } catch (JsonProcessingException e) {
            logger.error("transformAccessToken - failed to serialize to json", e, null, e);
        } catch (Exception e) {
            logger.error("transformAccessToken - failed", e, null, e);
        }

        return token;
    }

    public static ProtocolMapperModel create(String name,
            boolean accessToken, boolean idToken, boolean userInfo) {
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(PROVIDER_ID);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<String, String>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true"); // add custom mapper include in ACCESS
                                                                               // TOKEN
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        mapper.setConfig(config);
        return mapper;
    }

    // private String getAgronodAnvandereInfo(String userId) {
    // try {
    // String profileApiUrl = ConfigLoader.getInstance().getProperty("API.URL");

    // CloseableHttpClient client = HttpClients.createDefault();
    // HttpGet httpGet = new HttpGet(profileApiUrl +
    // "/rule/getApprovedAffarspartners");

    // // String json = getJsonstring(email, code);
    // // org.apache.http.entity.StringEntity entity = new
    // // org.apache.http.entity.StringEntity(json,
    // // ContentType.APPLICATION_JSON);
    // // httpGet.setEntity(entity);
    // httpGet.setHeader("Accept", "application/json");
    // httpGet.setHeader("Content-type", "application/json");

    // CloseableHttpResponse response = client.execute(httpGet);
    // client.close();
    // if (response.getStatusLine().getStatusCode() > 299) {
    // System.out.println(response.getStatusLine().getReasonPhrase());
    // return null;
    // }

    // BufferedReader in = new BufferedReader(new
    // InputStreamReader(response.getEntity().getContent()));
    // String inputLine;
    // StringBuffer content = new StringBuffer();
    // while ((inputLine = in.readLine()) != null) {
    // content.append(inputLine);
    // }

    // return content.toString();

    // } catch (Exception e) {
    // logger.error("Exception when calling Profile-api", e, null, e);
    // return null;
    // }
    // }
}