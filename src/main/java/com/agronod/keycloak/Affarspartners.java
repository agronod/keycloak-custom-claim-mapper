package com.agronod.keycloak;

import java.util.List;

public class Affarspartners {

    public String Id;
    public List<String> Roller = null;

    public Affarspartners(String id, List<String> roller) {
        Id = id;
        if (roller.size() > 0) {
            Roller = roller;
        }
    }
}