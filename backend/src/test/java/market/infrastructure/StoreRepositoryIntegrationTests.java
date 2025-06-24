package market.infrastructure;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import support.AcceptanceTestSpringBase;
import org.springframework.test.annotation.Commit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;



import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("market.infrastructure.StoreRepositoryIntegrationTests#isMySQLAvailable")
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

    

    @Test
    void testRemoveStoreAlsoCleansAssignments() throws Exception {
        String storeId = "888";
        Store store = new Store(storeId, "Delete Me", "founderDel", null);
        store.addNewManager("founderDel", "managerDel");

        storeRepository.addStore(store);
        storeRepository.removeStore("Delete Me");

        try (var conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bgu_market_test?useSSL=false", "bgu", "changeme");
            var ps = conn.prepareStatement(
                    "SELECT count(*) FROM store_assigners WHERE store_id = ?")
        ) {
            ps.setString(1, storeId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                int rows = rs.getInt(1);
                assertEquals(0, rows, "All assignments should be deleted with store");
            }
        }
    }

    @Test
    void testGetAllActiveStores_returnsOnlyOpenStores() throws Exception {
        Store activeStore = new Store("701", "Active Store", "founderA", null);
        Store closedStore = new Store("702", "Closed Store", "founderB", null);
        closedStore.setActive(false);

        storeRepository.addStore(activeStore);
        storeRepository.addStore(closedStore);

        List<Store> activeStores = storeRepository.getAllActiveStores();
        assertTrue(activeStores.stream().anyMatch(s -> s.getStoreID().equals("701")), "Active store should be included");
        assertTrue(activeStores.stream().noneMatch(s -> s.getStoreID().equals("702")), "Closed store should not be included");
    }

    






}
