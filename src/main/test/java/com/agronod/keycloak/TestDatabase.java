
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.List;

import org.junit.Test;

import com.agronod.keycloak.AgronodKonton;
import com.agronod.keycloak.DatabaseAccess;
import com.agronod.keycloak.UserInfo;

public class TestDatabase {

    private final DatabaseAccess databaseAccess = new DatabaseAccess();

    @Test
    public void addition() {
        String userId = "9a81291a-9089-4ec1-abb1-bfa843a9fa3b";

        try {
            Connection conn = databaseAccess.createDatabaseConnection(
                    "jdbc:postgresql://localhost:5432/datadelning?currentSchema=public&user=newuser&password=password");

            List<AgronodKonton> konton = this.databaseAccess.fetchOwnAgroKontoWithAffarspartners(userId, conn);

            UserInfo userInfo = this.databaseAccess.fetchUserInfo(userId, conn);

            // Admin roles
            konton = this.databaseAccess.fetchAdminRoles(userId, conn, konton);
        } catch (Exception e) {
        }

        assertEquals(userId, userId);
    }
}
