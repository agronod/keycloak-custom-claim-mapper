package com.agronod.keycloak;

import java.util.List;

public class Affarspartners {
    
    public String Id;
    public List<String> Roller;

    public Affarspartners(String id, List<String> roller) {
        Id = id;
        Roller = roller;
    }
}