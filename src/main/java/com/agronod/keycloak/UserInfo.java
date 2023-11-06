package com.agronod.keycloak;

import java.util.List;

public class UserInfo {

    public String Id;
    public List<String> Roller;

    public String email = null;
    public String name = null;
    public String ssn = null;

    public UserInfo(String email, String name, String ssn) {
        this.email = email;
        this.name = name;
        this.ssn = ssn;
    }
}