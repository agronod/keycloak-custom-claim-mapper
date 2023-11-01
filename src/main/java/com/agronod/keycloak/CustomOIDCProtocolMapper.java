package com.agronod.keycloak;

// import org.apache.http.client.methods.CloseableHttpResponse;
// import org.apache.http.client.methods.HttpGet;
// import org.apache.http.impl.client.CloseableHttpClient;
// import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;

// import com.agronod.keycloak.config.ConfigLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Array;
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomOIDCProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-customprotocolmapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static Logger logger = Logger.getLogger(CustomOIDCProtocolMapper.class);

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

    // jdbc:postgresql://localhost:5432/datadelning?currentSchema=public&user=newuser&password=password
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
            KeycloakSession keycloakSession,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        try {
            String userId = token.getSubject();
            Boolean isEmailVerified = token.getEmailVerified();
            String currentScope = token.getScope();

            final String connectionString = mappingModel.getConfig().get("connectionstring");

            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(connectionString);
            PreparedStatement st = conn.prepareStatement(
                    "select at2.id, at2.namn, ata.affarspartner_id, aar.anvandar_id, array_agg( aar.roll) from anvandare a "
                            +
                            "inner join agro_tenant at2 on a.agro_tenant_id  = at2.id  " +
                            "left outer join agro_tenant_affarspartner ata on at2.id = ata.agro_tenant_id  " +
                            "left outer join anvandare_affarspartner_roller aar on aar.anvandar_id = a.id and aar.affarspartner_id = ata.affarspartner_id "
                            +
                            "where externt_id = ? " +
                            "group by at2.id, at2.namn, ata.affarspartner_id, aar.anvandar_id ");
            st.setString(1, userId);
            ResultSet rs = st.executeQuery();

            List<AgronodKonton> konton = new ArrayList<AgronodKonton>();

            List<Affarspartners> affarspartners = new ArrayList<Affarspartners>();

            AgronodKonton konto = null;
            while (rs.next()) {
                konto = new AgronodKonton(rs.getString(1), rs.getString(2));
                Array test = rs.getArray(5);
                String[] roles = (String[]) test.getArray(); // TODO: so complicated...
                Affarspartners ap = new Affarspartners(rs.getString(3), roles);
                affarspartners.add(ap);
                logger.info("____ database returns: " + rs.getString(1));
            }
            konton.add(konto);
            konto.Affarspartners = affarspartners;

            rs.close();
            st.close();

            // Admin roles
            String adminRoles = "select ata.agro_tenant_id as agroTenantId, at2.namn as agroTenantName, aar.affarspartner_id as affarspartnerId, aar.anvandar_id as anvandarId , array_agg(aar.roll) as roller	" +
            "from anvandare a " +
            "inner join anvandare_affarspartner_roller aar on aar.anvandar_id = a.id " +
            "left outer join agro_tenant_affarspartner ata on ata.affarspartner_id = aar.affarspartner_id " +
            "left outer join agro_tenant at2 on at2.id = ata.agro_tenant_id " +
            "where a.externt_id = ? and a.agro_tenant_id != ata.agro_tenant_id and aar.roll = 'admin' " +
            "group by ata.agro_tenant_id , at2.namn , aar.affarspartner_id, aar.anvandar_id 	" +
            "order by ata.agro_tenant_id, aar.affarspartner_id;";


            logger.info("Fetched user info from Database");

            String jsonKonton = "";
            ObjectMapper mapper = new ObjectMapper();

            jsonKonton = mapper.writeValueAsString(konton);

            logger.info("____ konton: " + jsonKonton);

            token.getOtherClaims().put("argonodKonton", jsonKonton);
            // token.getOtherClaims().put("roller", "agro-admin");
            // if (currentScope.contains("ssn")) {
            // token.getOtherClaims().put("ssn", "28472748");
            // }

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