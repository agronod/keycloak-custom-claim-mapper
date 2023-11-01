package com.agronod.keycloak;

import java.util.List;

public class AgronodKonton {
    
    public String KontoId;
    public String KontoNamn;
    public List<com.agronod.keycloak.Affarspartners> Affarspartners;

    public AgronodKonton(String kontoId, String kontoNamn) {
        KontoId = kontoId;
        KontoNamn = kontoNamn;
    }

    // public String getAgroId() {
    //     return AgroId;
    // }

    // public void setAgroId(String mw) {
    //     AgroId = mw;
    // }
}
