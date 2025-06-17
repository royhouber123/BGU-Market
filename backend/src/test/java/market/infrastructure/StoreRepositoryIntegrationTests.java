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
    @Commit
    @Test
    void testAddRealStoreRow() throws Exception {
        Store store = new Store("1212", "Integration Success", "omertest", null);

        storeRepository.addStore(store);
    }

}
