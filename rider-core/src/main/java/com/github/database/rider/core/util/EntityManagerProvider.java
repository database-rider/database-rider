package com.github.database.rider.core.util;

/**
 * COPIED from JPA module because of maven cyclic dependencies (even with test scope)
 */

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;

public class EntityManagerProvider implements TestRule {

    private static Map<String, EntityManagerProvider> providers = new ConcurrentHashMap<>();//one emf per unit

    private EntityManagerFactory emf;

    private EntityManager em;

    private EntityTransaction tx;

    private Connection conn;

    private static PropertyResolutionUtil propertyResolutionUtil = new PropertyResolutionUtil();
    
    private static Map<String, String> overridingProperties;
    
    private static EntityManagerProvider instance;

    private static Logger log = LoggerFactory.getLogger(EntityManagerProvider.class);

    private EntityManagerProvider() {
    }
    
    public static synchronized EntityManagerProvider instance(String unitName) {
        instance = providers.get(unitName);
        if (instance == null) {
            instance = new EntityManagerProvider();
            providers.put(unitName,instance);
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
     * @param unitName unit name
     * @param overridingPersistenceProps properties to override persistence.xml props or define additions to them
     *
     * @return EntityManagerProvider instance
     */
    public static synchronized EntityManagerProvider instance(String unitName, Map<String,String> overridingPersistenceProps) {
        overridingProperties = overridingPersistenceProps;
        return instance(unitName);
    }
    
    /**
     * @param unitName unit name
     * clear entities on underlying context
     * @return a clean EntityManagerProvider
     */
    public static synchronized EntityManagerProvider newInstance(String unitName) {
        instance =  new EntityManagerProvider();
        providers.put(unitName,instance);
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
            Map<String,String> dbConfig = getDbPropertyConfig();
            log.debug("using dbConfig '{}' to create emf", dbConfig);
            emf = dbConfig == null ? Persistence.createEntityManagerFactory(unitName) : Persistence.createEntityManagerFactory(unitName, dbConfig);
            em =  emf.createEntityManager();
            tx = em.getTransaction();
            if (isHibernateOnClasspath() && em.getDelegate() instanceof Session) {
                conn = ((SessionImpl) em.unwrap(Session.class)).connection();
            } else{
                /**
                 * see here:http://wiki.eclipse.org/EclipseLink/Examples/JPA/EMAPI#Getting_a_JDBC_Connection_from_an_EntityManager
                 */
                tx.begin();
                conn = em.unwrap(Connection.class);
                tx.commit();
            }

        }
        emf.getCache().evictAll();
    }
    
    private Map<String, String> getDbPropertyConfig() {
        if(overridingProperties == null) {
            return propertyResolutionUtil.getSystemJavaxPersistenceOverrides();
        }
        
        return propertyResolutionUtil.persistencePropertiesOverrides(overridingProperties);
    }
    
    
    /**
     *
     * @param puName unit name
     * @return jdbc connection of provider instance represented by given puName
     */
    public Connection connection(String puName) {
        return instance(puName).conn;
    }

    /**
     *
     * @return jdbc conection of current provider instance
     */
    public Connection connection() {
        checkInstance();
        return instance.conn;
    }

    /**
     *
     * @param puName unit name
     * @return entityManager represented by given puName
     */
    public static EntityManager em(String puName) {
        return instance(puName).em;
    }

    /**
     *
     * @param puName unit name
     * @return entityManagerFactory represented by given puName
     */
    public static EntityManagerFactory emf(String puName) {
        return instance(puName).emf;
    }

    /**
     *
     * @return entityManager of current instance of this provider
     */
    public static EntityManager em() {
        checkInstance();
        return instance.em;
    }

    public EntityManager getEm() {
        return em();
    }

    public static EntityManagerFactory emf(){
        return instance.emf;
    }

    public EntityManagerFactory getEmf() {
        return instance.emf;
    }

    public EntityManager getEm(String puName){
        return em(puName);
    }

    /**
     * @param puName unit name
     * clears entityManager persistence context and entityManager factory cache represented by given puName
     * @return provider represented by puName
     */
    public static EntityManagerProvider clear(String puName){
        em(puName).clear();
        emf(puName).getCache().evictAll();
        return providers.get(puName);
    }

    /**
     * clears entityManager persistence context and entity manager factory cache of current instance of this provider
     * @return current provider
     */
    public static EntityManagerProvider clear(){
        em().clear();
        emf().getCache().evictAll();
        return instance;
    }

    /**
     * @param puName unit name
     * @return transaction of entityManager represented by given puName
     */
    public static EntityTransaction tx(String puName) {
        return em(puName).getTransaction();
    }

    /**
     *
     * @return transaction of entityManager of current instance of this provider
     */
    public static EntityTransaction tx() {
        checkInstance();
        return instance.tx;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    instance.em.clear();
                }
            }

        };
    }

    private boolean isHibernateOnClasspath() {
        return isOnClasspath("org.hibernate.Session");
    }

    private static void checkInstance() {
        if(instance == null){
            throw new IllegalStateException("Call instance('PU_NAME') before calling em()");
        }
    }
    public static boolean isEntityManagerActive(){
        return instance != null && em().isOpen();
    }
}