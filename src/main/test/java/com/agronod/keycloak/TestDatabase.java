
import java.sql.Connection;
import java.util.List;

import org.junit.Test;

import com.agronod.keycloak.AgronodKonton;
import com.agronod.keycloak.DatabaseAccess;
import com.agronod.keycloak.UserInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDatabase {

    private final DatabaseAccess databaseAccess = new DatabaseAccess();

    @Test
    public void addition() {
        String userId = "e3671d30-20e8-47f9-b236-831b887f3f32";
        String jsonKonton = "";

        try {
            Connection conn = databaseAccess.createDatabaseConnection(
                    "jdbc:postgresql://localhost:5432/datadelning?currentSchema=public&user=newuser&password=password");

            List<AgronodKonton> konton = this.databaseAccess.fetchOwnAgroKontoWithAffarspartners(userId, conn);

            UserInfo userInfo = this.databaseAccess.fetchUserInfo(userId, conn);

            // Admin roles
            konton = this.databaseAccess.fetchAdminRoles(userId, conn, konton);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            jsonKonton = mapper.writeValueAsString(konton);

        } catch (Exception e) {
        }

        // assertEquals(userId, jsonKonton);
    }
}
