package com.agronod.keycloak;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.logging.Logger;

public class DatabaseAccess {

    final String adminRolesQuery = "select ata.agronodkonto_id, at2.namn, aar.affarspartner_id, coalesce(array_agg( aar.roll)FILTER (WHERE aar.roll IS NOT NULL),'{}') "
            +
            "from anvandare a " +
            "inner join anvandare_affarspartner_roller aar on aar.anvandar_id = a.id " +
            "left outer join agronodkonto_affarspartner ata on ata.affarspartner_id = aar.affarspartner_id " +
            "left outer join agronodkonto at2 on at2.id = ata.agronodkonto_id " +
            "where a.externt_id = ? and a.agronodkonto_id != ata.agronodkonto_id and aar.roll in ('admin','support') " +
            "group by ata.agronodkonto_id , at2.namn , aar.affarspartner_id, aar.anvandar_id 	" +
            "order by ata.agronodkonto_id, aar.affarspartner_id;";

    private static Logger logger = Logger.getLogger(DatabaseAccess.class);

    public List<AgronodKonton> fetchAdminRoles(Connection conn, String userId, List<AgronodKonton> konton) {
        AgronodKonton konto;

        try {

            PreparedStatement st3 = conn.prepareStatement(adminRolesQuery);
            st3.setString(1, userId);
            ResultSet rs3 = st3.executeQuery();

            while (rs3.next()) {

                String agroTenantId = rs3.getString(1);
                String agroTenantName = rs3.getString(2);
                String affarspartnerId = rs3.getString(3);
                Array dbRoles = rs3.getArray(4);

                List<String> roles = dbRoles != null ? Arrays.asList((String[]) dbRoles.getArray())
                        : new ArrayList<String>();

                AgronodKonton existingKonto = findKonto(agroTenantId, konton);
                if (existingKonto != null) {
                    // AgronodKonto finns
                    Affarspartners existingAffPartner = findAffarspartner(affarspartnerId,
                            existingKonto.Affarspartners);
                    Affarspartners ap = new Affarspartners(affarspartnerId, roles);

                    if (existingAffPartner != null) {
                        // Affarspartner finns
                    } else {
                        if (existingKonto.Affarspartners == null) {
                            existingKonto.Affarspartners = new ArrayList<Affarspartners>();
                        }
                        existingKonto.Affarspartners.add(ap);
                    }
                } else {
                    konto = new AgronodKonton(agroTenantId, agroTenantName);
                    if (affarspartnerId != null) {
                        Affarspartners ap = new Affarspartners(affarspartnerId, roles);
                        konto.Affarspartners = new ArrayList<Affarspartners>();
                        konto.Affarspartners.add(ap);
                    }
                    konton.add(konto);
                }
            }

            rs3.close();
            st3.close();
        } catch (Exception e) {
            logger.error("Error fetching admin roles for userId:" + userId, e);
        }
        return konton;
    }

    public List<AgronodKonton> fetchOwnAgroKontoWithAffarspartners(Connection conn, String userId) {

        List<AgronodKonton> konton = new ArrayList<AgronodKonton>();
        List<Affarspartners> affarspartners = new ArrayList<Affarspartners>();
        AgronodKonton konto;

        try {
            PreparedStatement st = conn.prepareStatement(
                    "select at2.id, at2.namn, ata.affarspartner_id, coalesce(array_agg( aar.roll)FILTER (WHERE aar.roll IS NOT NULL),'{}') from anvandare a "
                            +
                            "inner join agronodkonto at2 on a.agronodkonto_id  = at2.id and at2.registrerad is true " +
                            "left outer join agronodkonto_affarspartner ata on at2.id = ata.agronodkonto_id  " +
                            "left outer join anvandare_affarspartner_roller aar on aar.anvandar_id = a.id and aar.affarspartner_id = ata.affarspartner_id "
                            +
                            "where externt_id = ? " +
                            "group by at2.id, at2.namn, ata.affarspartner_id, aar.anvandar_id ");
            st.setString(1, userId);
            ResultSet rs = st.executeQuery();

            konto = null;

            while (rs.next()) {
                // TODO: This assumes that a user only can have ONE agronodKonto (AgroTenant)
                konto = new AgronodKonton(rs.getString(1), rs.getString(2));
                Array dbRoles = rs.getArray(4);
                List<String> roles = dbRoles != null ? Arrays.asList((String[]) dbRoles.getArray())
                        : new ArrayList<String>();

                String affarspartnerId = rs.getString(3);
                if (affarspartnerId != null) {
                    Affarspartners ap = new Affarspartners(rs.getString(3), roles);
                    affarspartners.add(ap);
                }
            }
            if (konto != null) {
                konton.add(konto);
                if (affarspartners.size() > 0) {
                    konto.Affarspartners = affarspartners;
                }
            }

            rs.close();
            st.close();
        } catch (Exception e) {
            logger.error("Error fetching own affarspartners for userId:" + userId, e);
        }
        return konton;
    }

    public UserInfo fetchUserInfo(Connection conn, String userId) {
        String email = null;
        String name = null;
        String ssn = null;
        String id = null;
        boolean registrerad = false;

        try {
            PreparedStatement st2 = conn.prepareStatement(
                    "select namn, personnummer, epost, registrerad, id from anvandare where externt_id = ?");
            st2.setString(1, userId);
            ResultSet rs2 = st2.executeQuery();

            while (rs2.next()) {
                name = rs2.getString(1);
                ssn = rs2.getString(2);
                email = rs2.getString(3);
                registrerad = rs2.getBoolean(4);
                id = rs2.getString(5);
            }

            rs2.close();
            st2.close();
        } catch (Exception e) {
            logger.error("Error fetching own user info for userId:" + userId, e);
        }
        return new UserInfo(email, name, ssn, registrerad, id);
    }

    public List<AgronodKonton> fetchAgronodKontoRolesAndAddToKontoList(Connection conn, String userId, List<AgronodKonton> konton) {
        AgronodKonton konto;

        try {

            String agronodkontoRolesQuery = "select aa.agronodkonto_id, at2.namn, aa.affarspartner_id, coalesce(array_agg( aar.roll)FILTER (WHERE aar.roll IS NOT NULL),'{}') " +
                        "from anvandare a " +
                        "inner join anvandare_agronodkonto_roller aar on aar.anvandar_id = a.id " +
                        "left outer join agronodkonto at2 on at2.id = aar.agronodkonto_id " +
                        "left outer join agronodkonto_affarspartner aa on aa.agronodkonto_id = aar.agronodkonto_id " +
                        "where  " +
                        "a.externt_id = ? " +
                        "and a.agronodkonto_id != aa.agronodkonto_id " +
                        "group by a.externt_id, aa.agronodkonto_id , at2.namn , aa.affarspartner_id, aar.anvandar_id  " +
                        "order by aa.agronodkonto_id, aa.affarspartner_id;";

            PreparedStatement st3 = conn.prepareStatement(agronodkontoRolesQuery);
            st3.setString(1, userId);
            ResultSet rs3 = st3.executeQuery();

            while (rs3.next()) {

                String agroTenantId = rs3.getString(1);
                String agroTenantName = rs3.getString(2);
                String affarspartnerId = rs3.getString(3);
                Array dbRoles = rs3.getArray(4);

                List<String> roles = dbRoles != null ? Arrays.asList((String[]) dbRoles.getArray())
                        : new ArrayList<String>();

                AgronodKonton existingKonto = findKonto(agroTenantId, konton);
                if (existingKonto != null) {
                    // AgronodKonto finns
                    Affarspartners existingAffPartner = findAffarspartner(affarspartnerId,
                            existingKonto.Affarspartners);
                    Affarspartners ap = new Affarspartners(affarspartnerId, roles);

                    if (existingAffPartner != null) {
                        // Affarspartner finns
                    } else {
                        if (existingKonto.Affarspartners == null) {
                            existingKonto.Affarspartners = new ArrayList<Affarspartners>();
                        }
                        existingKonto.Affarspartners.add(ap);
                    }
                } else {
                    konto = new AgronodKonton(agroTenantId, agroTenantName);
                    if (affarspartnerId != null) {
                        Affarspartners ap = new Affarspartners(affarspartnerId, roles);
                        konto.Affarspartners = new ArrayList<Affarspartners>();
                        konto.Affarspartners.add(ap);
                    }
                    konton.add(konto);
                }
            }

            rs3.close();
            st3.close();
        } catch (Exception e) {
            logger.error("Error fetching admin roles for userId:" + userId, e);
        }
        return konton;
    }

    private AgronodKonton findKonto(String kontoId, List<AgronodKonton> agroKonton) {
        Iterator<AgronodKonton> iterator = agroKonton.iterator();
        while (iterator.hasNext()) {
            AgronodKonton konto = iterator.next();
            if (konto.KontoId.equals(kontoId)) {
                return konto;
            }
        }
        return null;
    }

    private Affarspartners findAffarspartner(String id, List<Affarspartners> affarspartners) {
        Iterator<Affarspartners> iterator = affarspartners.iterator();
        while (iterator.hasNext()) {
            Affarspartners ap = iterator.next();
            if (ap.Id.equals(id)) {
                return ap;
            }
        }
        return null;
    }

}
