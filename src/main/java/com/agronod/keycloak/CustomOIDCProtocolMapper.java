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

import com.agronod.keycloak.config.ConfigLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomOIDCProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-customprotocolmapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static Logger logger = Logger.getLogger(CustomOIDCProtocolMapper.class);
    
    /**
     * Maybe you want to have config fields for your Mapper
     */
    /*
     * static {
     * ProviderConfigProperty property;
     * property = new ProviderConfigProperty();
     * property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
     * property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
     * property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
     * property.setType(ProviderConfigProperty.STRING_TYPE);
     * configProperties.add(property);
     * property = new ProviderConfigProperty();
     * property.setName(ProtocolMapperUtils.MULTIVALUED);
     * property.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
     * property.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
     * property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
     * configProperties.add(property);
     * }
     */
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
        String env = ConfigLoader.getInstance().getProperty("BUILD.ENV");

        return "Adds claims from Agronods User/claims API for " + env;
    }

    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
            KeycloakSession keycloakSession,
            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {

String subject = token.getSubject();
String id = token.getId();
System.console().writer().println("________ id: " + id + " subject: " + subject);
logger.info("________ id: " + id + " subject: " + subject);


        token.getOtherClaims().put("agronod_roles",
                "8313d61d-7c93-46e4-931a-191c51fff8da_394797-ff434-3453-345_firmatecknare, 8313d61d-7c93-46e4-931a-191c51fff8da_394797-ff434-3453-345_radgivare");
        setClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);
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
}