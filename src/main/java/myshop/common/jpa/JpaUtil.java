package myshop.common.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaUtil {
    private static final String PERSISTENCE_UNIT_NAME = "MyShopPU";
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = buildEntityManagerFactory();

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            log.info("Initializing EntityManagerFactory for persistence unit '{}'", PERSISTENCE_UNIT_NAME);
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Exception e) {
            log.error("Error creating EntityManagerFactory", e);
            throw new RuntimeException("Failed to initialize EntityManagerFactory", e);
        }
    }

    public static EntityManager getEntityManager() {
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }
}
