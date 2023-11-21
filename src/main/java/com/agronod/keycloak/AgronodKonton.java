package com.agronod.keycloak;

import java.util.List;

public class AgronodKonton {

    public String KontoId;
    public String KontoNamn;
    public List<com.agronod.keycloak.Affarspartners> Affarspartners = null;

    public AgronodKonton(String kontoId, String kontoNamn) {
        KontoId = kontoId;
        KontoNamn = kontoNamn;
    }
}
