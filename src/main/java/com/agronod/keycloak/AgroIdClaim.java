package com.agronod.keycloak;

public class AgroIdClaim {
    
    public String AgroId;
    public String[] AffarspartnerIds;

    public AgroIdClaim(String agroId, String[] affarspartnerIds) {
        AgroId = agroId;
        AffarspartnerIds = affarspartnerIds;
    }

    // public String getAgroId() {
    //     return AgroId;
    // }

    // public void setAgroId(String mw) {
    //     AgroId = mw;
    // }
}