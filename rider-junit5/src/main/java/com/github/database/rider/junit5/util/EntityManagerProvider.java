package com.github.database.rider.junit5.util;

import com.github.database.rider.core.util.PropertyResolutionUtil;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;
/**
 * This class is set to @Deprecated because it's duplicated from {@link com.github.database.rider.core.util.EntityManagerProvider}
 * It will be removed with the database-rider 2.0.0 release <br/>
 *
 * You can use {@link com.github.database.rider.core.util.EntityManagerProvider} for testing purposes. <br/>
 * Use in this case temporarily {@link com.github.database.rider.junit5.incubating.DBUnitExtension} instead of {@link com.github.database.rider.junit5.DBUnitExtension} <br/>
 * And use {@link com.github.database.rider.junit5.incubating.DBRider} instead of {@link com.github.database.rider.junit5.api.DBRider} <br/>
 * DBRiderExtension and @Rider are help classes to ensure backwards compatibility during the <2.0.0 release and will be removed in the 2.0.0 Release.
 * */
@Deprecated
public class EntityManagerProvider {

    private static Map<String, EntityManagerProvider> providers = new ConcurrentHashMap<>();//one emf per unit

    private EntityManagerFactory emf;

    private EntityManager em;

    private EntityTransaction tx;

    private Connection conn;

    private static PropertyResolutionUtil propertyResolutionUtil = new PropertyResolutionUtil();

    private static Map<String, Object> overridingProperties;

    private static EntityManagerProvider instance;

    private static Logger log = LoggerFactory.getLogger(EntityManagerProvider.class);

    private EntityManagerProvider() {
    }

    public static synchronized EntityManagerProvider instance(String unitName) {
        instance = providers.get(unitName);
        if (instance == null) {
            instance = new EntityManagerProvider();
            providers.put(unitName, instance);
        }
        try {
            instance.init(unitName);
        } catch (Exception e) {
            log.error("Could not initialize persistence unit " + unitName, e);
        }

        return instance;
    }

    /**
     * Allows to pass in overriding Properties that may be specific to the JPA Vendor.
     *
     * @param unitName                   unit name
     * @param overridingPersistenceProps properties to override persistence.xml props or define additions to them
     * @return EntityManagerProvider instance
     */
    public static synchronized EntityManagerProvider instance(String unitName,
                                                              Map<String, Object> overridingPersistenceProps) {
        overridingProperties = overridingPersistenceProps;
        return instance(unitName);
    }

    /**
     * @param unitName unit name
     *                 clear entities on underlying context
     * @return a clean EntityManagerProvider
     */
    public static synchronized EntityManagerProvider newInstance(String unitName) {
        instance = new EntityManagerProvider();
        providers.put(unitName, instance);
        try {
            instance.init(unitName);
        } catch (Exception e) {
            log.error("Could not initialize persistence unit " + unitName, e);
        }

        return instance;
    }

    private void init(String unitName) {
        if (emf == null) {
            log.debug("creating emf for unit {}", unitName);
            Map<String, Object> dbConfig = getDbPropertyConfig();
            log.debug("using dbConfig '{}' to create emf", dbConfig);
            emf = dbConfig == null ?
                    Persistence.createEntityManagerFactory(unitName) :
                    Persistence.createEntityManagerFactory(unitName, dbConfig);
            em = emf.createEntityManager();
            conn = createConnection(em);
            tx = em.getTransaction();
        }
        emf.getCache().evictAll();
    }

    private Connection createConnection(EntityManager em) {
        Connection connection;
        final EntityTransaction tx = em.getTransaction();
        if (isHibernateOnClasspath() && em.getDelegate() instanceof Session) {
            connection = ((SessionImpl) em.unwrap(Session.class)).connection();
        } else {
            /**
             * see here:http://wiki.eclipse.org/EclipseLink/Examples/JPA/EMAPI#Getting_a_JDBC_Connection_from_an_EntityManager
             */
            tx.begin();
            connection = em.unwrap(Connection.class);
            tx.commit();
        }
        return connection;
    }

    private Map<String, Object> getDbPropertyConfig() {
        if (overridingProperties == null) {
            return propertyResolutionUtil.getSystemJavaxPersistenceOverrides();
        }

        return propertyResolutionUtil.persistencePropertiesOverrides(overridingProperties);
    }

    /**
     * @param puName unit name
     * @return jdbc connection of provider instance represented by given puName
     */
    public Connection connection(String puName) {
        return instance(puName).conn;
    }

    /**
     * @return jdbc conection of current provider instance
     */
    public Connection connection() {
        checkInstance();
        try {
            if (instance.conn == null || instance.conn.isClosed()) {
                instance.conn = createConnection(instance.em);
            }
        } catch (SQLException e) {
            log.error("Could not create new jdbc connection.", e);
        }
        return instance.conn;
    }

    /**
     * @param puName unit name
     * @return entityManager represented by given puName
     */
    public static EntityManager em(String puName) {
        return instance(puName).em;
    }

    /**
     * @param puName unit name
     * @return entityManagerFactory represented by given puName
     */
    public static EntityManagerFactory emf(String puName) {
        return instance(puName).emf;
    }

    /**
     * @return entityManager of current instance of this provider
     */
    public static EntityManager em() {
        checkInstance();
        return instance.em;
    }

    public EntityManager getEm() {
        return em();
    }

    public static EntityManagerFactory emf() {
        return instance.emf;
    }

    public EntityManagerFactory getEmf() {
        return instance.emf;
    }

    public EntityManager getEm(String puName) {
        return em(puName);
    }

    /**
     * @param puName unit name clears entityManager persistence context and entityManager factory cache represented by
     *               given puName
     * @return provider represented by puName
     */
    public static EntityManagerProvider clear(String puName) {
        em(puName).clear();
        emf(puName).getCache().evictAll();
        return providers.get(puName);
    }

    /**
     * clears entityManager persistence context and entity manager factory cache of current instance of this provider
     *
     * @return current provider
     */
    public static EntityManagerProvider clear() {
        if (isEntityManagerActive()) {
            em().clear();
            emf().getCache().evictAll();
            return instance;
        }
        return null;
    }

    /**
     * @param puName unit name
     * @return transaction of entityManager represented by given puName
     */
    public static EntityTransaction tx(String puName) {
        return em(puName).getTransaction();
    }

    /**
     * @return transaction of entityManager of current instance of this provider
     */
    public static EntityTransaction tx() {
        checkInstance();
        return instance.tx;
    }

    private boolean isHibernateOnClasspath() {
        return isOnClasspath("org.hibernate.Session");
    }

    private static void checkInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call instance('PU_NAME') before calling em()");
        }
    }

    public static boolean isEntityManagerActive() {
        return instance != null && em().isOpen();
    }
}