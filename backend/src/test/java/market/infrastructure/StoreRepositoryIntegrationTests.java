package market.infrastructure;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import support.AcceptanceTestSpringBase;
import org.springframework.test.annotation.Commit;


import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class StoreRepositoryIntegrationTests extends AcceptanceTestSpringBase {

    @Autowired
    private IStoreRepository storeRepository;

    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL not available – skipping StoreRepositoryIntegrationTests");
            return false;
        }
    }
    
    @Test
    void testAddRealStoreRow() throws Exception {
        Store store = new Store("250", "Integration Success", "omertest", null);
        storeRepository.addStore(store);

        Store retrieved = storeRepository.getStoreByID("250");
        assertNotNull(retrieved, "Store should be found in the repository");
        assertEquals("Integration Success", retrieved.getName());
    }

    @Commit
    @Test
    void step1_addManagerToStore() throws Exception {
        Store store = new Store("111", "Test Store", "omer", null);
        store.addNewManager("omer", "testmanager");
        storeRepository.addStore(store);
    }

    @Test
    void step2_verifyManagerPersisted() throws Exception {
        try (var conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bgu_market_test?useSSL=false", "bgu", "changeme");
             var ps = conn.prepareStatement(
                     "SELECT count(*) FROM store_assigners WHERE store_id=? AND assigner=? AND assignee=?")
        ) {
            ps.setString(1, "111");
            ps.setString(2, "omer");
            ps.setString(3, "testmanager");

            try (var rs = ps.executeQuery()) {
                rs.next();
                int rows = rs.getInt(1);
                assertEquals(1, rows, "Assignment row should exist in store_assigners table");
            }
        }
    }


}
