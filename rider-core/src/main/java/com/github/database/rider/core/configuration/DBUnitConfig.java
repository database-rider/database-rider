package com.github.database.rider.core.configuration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.DataSetMergingStrategy;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.connection.RiderDataSource;
import com.github.database.rider.core.dataset.DataSetExecutorImpl;
import com.github.database.rider.core.replacers.DateTimeReplacer;
import com.github.database.rider.core.replacers.NullReplacer;
import com.github.database.rider.core.replacers.Replacer;
import com.github.database.rider.core.replacers.UnixTimestampReplacer;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import static com.github.database.rider.core.configuration.DBUnitConfigPropertyResolver.resolveProperties;
import static com.github.database.rider.core.configuration.DBUnitConfigPropertyResolver.resolveProperty;

/**
 * represents DBUnit configuration of a dataset executor.
 */
public class DBUnitConfig {

    private String executorId;
    private Boolean cacheConnection;
    private Boolean cacheTableNames;
    private Boolean leakHunter;
    private Boolean mergeDataSets;
    private Boolean columnSensing;
    private Boolean raiseExceptionOnCleanUp;
    private Boolean disableSequenceFiltering;
    private Boolean alwaysCleanBefore;
    private Boolean alwaysCleanAfter;
    private Orthography caseInsensitiveStrategy;
    private String[] disablePKCheckFor;
    private DataSetMergingStrategy mergingStrategy;
    private RiderDataSource.DBType expectedDbType;
    private Map<String, Object> properties;
    private ConnectionConfig connectionConfig;

    public DBUnitConfig() {
        this(DataSetExecutorImpl.DEFAULT_EXECUTOR_ID);
    }

    public DBUnitConfig(String executor) {
        this.executorId = executor;
        initDefault();
    }

    private void initDefault() {
        if ("".equals(executorId)) {
            executorId = DataSetExecutorImpl.DEFAULT_EXECUTOR_ID;
        }
        cacheConnection = true;
        cacheTableNames = true;
        leakHunter = false;
        caseInsensitiveStrategy = Orthography.UPPERCASE;
        mergingStrategy = DataSetMergingStrategy.METHOD;
        mergeDataSets = Boolean.FALSE;
        columnSensing = Boolean.FALSE;
        raiseExceptionOnCleanUp = Boolean.FALSE;
        disableSequenceFiltering = Boolean.FALSE;
        alwaysCleanBefore = Boolean.FALSE;
        alwaysCleanAfter = Boolean.FALSE;
        expectedDbType = RiderDataSource.DBType.UNKNOWN;
        initDefaultProperties();
        initDefaultConnectionConfig();
    }

    private void initDefaultProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        putIfAbsent(properties, "batchedStatements", false);
        putIfAbsent(properties, "qualifiedTableNames", false);
        putIfAbsent(properties, "schema", null);
        putIfAbsent(properties, "caseSensitiveTableNames", false);
        putIfAbsent(properties, "batchSize", 100);
        putIfAbsent(properties, "fetchSize", 100);
        putIfAbsent(properties, "allowEmptyFields", false);
        putIfAbsent(properties, "replacers", new ArrayList<>(
                Arrays.asList(new DateTimeReplacer(), new UnixTimestampReplacer(), new NullReplacer())));
        putIfAbsent(properties, "tableType", Collections.singletonList("TABLE"));
        putIfAbsent(properties, "prologTimeout", 1_000L);
    }

    private <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    private void initDefaultConnectionConfig() {
        if (connectionConfig == null) {
            connectionConfig = new ConnectionConfig();
        }
        if (connectionConfig.getDriver() == null) {
            connectionConfig.setDriver("");
        }
        if (connectionConfig.getUrl() == null) {
            connectionConfig.setUrl("");
        }
        if (connectionConfig.getUser() == null) {
            connectionConfig.setUser("");
        }
        if (connectionConfig.getPassword() == null) {
            connectionConfig.setPassword("");
        }
    }

    public static DBUnitConfig fromCustomGlobalFile() {
        try (InputStream customConfiguration = Thread.currentThread().getContextClassLoader().getResourceAsStream("dbunit.yml")) {
            if (customConfiguration != null) {
                DBUnitConfig configFromFile = new Yaml().loadAs(customConfiguration, DBUnitConfig.class);
                configFromFile.initDefaultProperties();
                configFromFile.initDefaultConnectionConfig();
                return configFromFile;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't load config from global file", e);
        }

        return new DBUnitConfig();
    }

    public static DBUnitConfig from(DBUnit dbUnit) {
        DBUnitConfig dbUnitConfig = new DBUnitConfig(dbUnit.executor());

        dbUnitConfig.cacheConnection(dbUnit.cacheConnection())
                .cacheTableNames(dbUnit.cacheTableNames())
                .leakHunter(dbUnit.leakHunter())
                .mergeDataSets(dbUnit.mergeDataSets())
                .columnSensing(dbUnit.columnSensing())
                .raiseExceptionOnCleanUp(dbUnit.raiseExceptionOnCleanUp())
                .disableSequenceFiltering(dbUnit.disableSequenceFiltering())
                .alwaysCleanBefore(dbUnit.alwaysCleanBefore())
                .alwaysCleanAfter(dbUnit.alwaysCleanAfter())
                .expectedDbType(dbUnit.expectedDbType())
                .caseInsensitiveStrategy(dbUnit.caseInsensitiveStrategy())
                .mergingStrategy(dbUnit.mergingStrategy())
                .disablePKCheckFor(dbUnit.disablePKCheckFor())
                .addDBUnitProperty("batchedStatements", dbUnit.batchedStatements())
                .addDBUnitProperty("batchSize", dbUnit.batchSize())
                .addDBUnitProperty("allowEmptyFields", dbUnit.allowEmptyFields())
                .addDBUnitProperty("fetchSize", dbUnit.fetchSize())
                .addDBUnitProperty("qualifiedTableNames", dbUnit.qualifiedTableNames())
                .addDBUnitProperty("schema", !dbUnit.schema().isEmpty() ? dbUnit.schema() : null)
                .addDBUnitProperty("caseSensitiveTableNames", dbUnit.caseSensitiveTableNames())
                .addDBUnitProperty("tableType", dbUnit.tableType())
                .addDBUnitProperty("prologTimeout", dbUnit.prologTimeout());


        if (!"".equals(dbUnit.escapePattern())) {
            dbUnitConfig.addDBUnitProperty("escapePattern", dbUnit.escapePattern());
        }

        if (!dbUnit.dataTypeFactoryClass().isInterface()) {
            try {
                IDataTypeFactory factory = dbUnit.dataTypeFactoryClass().newInstance();
                dbUnitConfig.addDBUnitProperty("datatypeFactory", factory);
            } catch (Exception e) {
                throw new RuntimeException("failed to instantiate datatypeFactory", e);
            }
        }

        if (!dbUnit.metaDataHandler().isInterface()) {
            try {
                IMetadataHandler factory = dbUnit.metaDataHandler().newInstance();
                dbUnitConfig.addDBUnitProperty("metadataHandler", factory);
            } catch (Exception e) {
                throw new RuntimeException("failed to instantiate metadataHandler", e);
            }
        }

        List<Replacer> dbUnitReplacers = new ArrayList<>();
        for (Class<? extends Replacer> replacerClass : dbUnit.replacers()) {
            try {
                dbUnitReplacers.add(replacerClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(replacerClass.getName() + " could not be instantiated as Replacer");
            }
        }

        @SuppressWarnings("unchecked") List<Replacer> defaultReplacers = (List<Replacer>) dbUnitConfig.getProperties().get("replacers");
        if (defaultReplacers != null && defaultReplacers.size() > 0) {
            // merge replacers
            dbUnitReplacers.addAll(defaultReplacers);
        }

        dbUnitConfig.addDBUnitProperty("replacers", dbUnitReplacers);

        // declarative connection config
        dbUnitConfig.driver(dbUnit.driver())
                .url(dbUnit.url())
                .user(dbUnit.user())
                .password(dbUnit.password());

        return dbUnitConfig;
    }

    public static DBUnitConfig fromGlobalConfig() {
        return GlobalConfig.instance().getDbUnitConfig();
    }

    public static DBUnitConfig from(Method method) {
        DBUnit dbUnitConfig = method.getAnnotation(DBUnit.class);
        if (dbUnitConfig == null) {
            dbUnitConfig = method.getDeclaringClass().getAnnotation(DBUnit.class);
        }
        if (dbUnitConfig != null) {
            return from(dbUnitConfig);
        } else {
            return fromGlobalConfig();
        }
    }

    public DBUnitConfig cacheConnection(boolean cacheConnection) {
        this.cacheConnection = cacheConnection;
        return this;
    }

    public DBUnitConfig executorId(String executorId) {
        this.executorId = executorId;
        return this;
    }

    public DBUnitConfig leakHunter(boolean leakHunter) {
        this.leakHunter = leakHunter;
        return this;
    }

    public DBUnitConfig cacheTableNames(boolean cacheTables) {
        this.cacheTableNames = cacheTables;
        return this;
    }

    public DBUnitConfig mergeDataSets(boolean mergeDataSets) {
        this.mergeDataSets = mergeDataSets;
        return this;
    }

    private DBUnitConfig disableSequenceFiltering(boolean disableSequenceFiltering) {
        this.disableSequenceFiltering = disableSequenceFiltering;
        return this;
    }

    private DBUnitConfig alwaysCleanBefore(boolean alwaysCleanBefore) {
        this.alwaysCleanBefore = alwaysCleanBefore;
        return this;
    }

    private DBUnitConfig alwaysCleanAfter(boolean alwaysCleanAfter) {
        this.alwaysCleanAfter = alwaysCleanAfter;
        return this;
    }

    public DBUnitConfig columnSensing(boolean columnSensing) {
        this.columnSensing = columnSensing;
        return this;
    }

    public DBUnitConfig caseInsensitiveStrategy(Orthography orthography) {
        this.caseInsensitiveStrategy = orthography;
        return this;
    }

    public DBUnitConfig mergingStrategy(DataSetMergingStrategy dataSetMergingStrategy) {
        this.mergingStrategy = dataSetMergingStrategy;
        return this;
    }

    public DBUnitConfig addDBUnitProperty(String name, Object value) {
        properties.put(name, resolveProperty(value));
        return this;
    }

    public DBUnitConfig driver(String driverClass) {
        connectionConfig.setDriver(driverClass);
        return this;
    }

    public DBUnitConfig url(String url) {
        connectionConfig.setUrl(url);
        return this;
    }

    public DBUnitConfig user(String user) {
        connectionConfig.setUser(user);
        return this;
    }

    public DBUnitConfig password(String password) {
        connectionConfig.setPassword(password);
        return this;
    }

    public DBUnitConfig raiseExceptionOnCleanUp(boolean raiseExceptionOnCleanUp) {
        this.raiseExceptionOnCleanUp = raiseExceptionOnCleanUp;
        return this;
    }

    public DBUnitConfig disablePKCheckFor(String... tables) {
        disablePKCheckFor = tables;
        return this;
    }

    public DBUnitConfig expectedDbType(RiderDataSource.DBType expectedDbType) {
        this.expectedDbType = expectedDbType;
        return this;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    // methods below are for snakeyml library

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = resolveProperties(properties);
    }

    public Boolean isCacheTableNames() {
        return cacheTableNames;
    }

    public void setCacheTableNames(boolean cacheTableNames) {
        this.cacheTableNames = cacheTableNames;
    }

    public Boolean isCacheConnection() {
        return cacheConnection;
    }

    public void setCacheConnection(boolean cacheConnection) {
        this.cacheConnection = cacheConnection;
    }

    public Boolean isMergeDataSets() {
        return mergeDataSets;
    }

    public void setMergeDataSets(Boolean mergeDataSets) {
        this.mergeDataSets = mergeDataSets;
    }

    public Boolean isColumnSensing() {
        return columnSensing;
    }

    public void setColumnSensing(boolean columnSensing) {
        this.columnSensing = columnSensing;
    }

    public Boolean isLeakHunter() {
        return leakHunter;
    }

    public void setLeakHunter(boolean activateLeakHunter) {
        this.leakHunter = activateLeakHunter;
    }

    public Orthography getCaseInsensitiveStrategy() {
        return caseInsensitiveStrategy;
    }

    public void setCaseInsensitiveStrategy(Orthography caseInsensitiveStrategy) {
        this.caseInsensitiveStrategy = caseInsensitiveStrategy;
    }

    public DataSetMergingStrategy getMergingStrategy() {
        return mergingStrategy;
    }

    public void setMergingStrategy(DataSetMergingStrategy mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public String getExecutorId() {
        return executorId;
    }

    public boolean isCaseSensitiveTableNames() {
        return properties.containsKey("caseSensitiveTableNames") && Boolean.parseBoolean(properties.get("caseSensitiveTableNames").toString());
    }

    public Long getPrologTimeout() {
        return (Long) properties.getOrDefault("prologTimeout", 1_000L);
    }

    public String[] getDisablePKCheckFor() {
        return disablePKCheckFor;
    }

    public void setDisablePKCheckFor(String[] disablePKCheckFor) {
        this.disablePKCheckFor = disablePKCheckFor;
    }

    public String getSchema() {
        return (String) properties.get("schema");
    }

    public boolean isRaiseExceptionOnCleanUp() {
        return raiseExceptionOnCleanUp;
    }

    public void setRaiseExceptionOnCleanUp(boolean raiseExceptionOnCleanUp) {
        this.raiseExceptionOnCleanUp = raiseExceptionOnCleanUp;
    }

    public Boolean isDisableSequenceFiltering() {
        return disableSequenceFiltering;
    }

    public void setDisableSequenceFiltering(Boolean disableSequenceFiltering) {
        this.disableSequenceFiltering = disableSequenceFiltering;
    }

    public Boolean isAlwaysCleanBefore() {
        return alwaysCleanBefore;
    }

    public void setAlwaysCleanBefore(Boolean alwaysCleanBefore) {
        this.alwaysCleanBefore = alwaysCleanBefore;
    }

    public Boolean isAlwaysCleanAfter() {
        return alwaysCleanAfter;
    }

    public void setAlwaysCleanAfter(Boolean alwaysCleanAfter) {
        this.alwaysCleanAfter = alwaysCleanAfter;
    }

    public RiderDataSource.DBType getExpectedDbType() {
        return expectedDbType;
    }

    public void setExpectedDbType(RiderDataSource.DBType expectedDbType) {
        this.expectedDbType = expectedDbType;
    }

    public static class Constants {
        public static final String SEQUENCE_TABLE_NAME;
        public static final EnumMap<RiderDataSource.DBType, Set<String>> SYSTEM_SCHEMAS = new EnumMap<>(RiderDataSource.DBType.class);
        public static final Set<String> RESERVED_TABLE_NAMES = new HashSet<>();
        public static final String DATASETS_FOLDER = "datasets/";

        /**
         * ISO 9075 SQL Standard Keywords/Reserved Words. Taken from
         * https://firebirdsql.org/en/iso-9075-sql-standard-keywords-reserved-words/
         */
        public static final String DEFAULT_SQL_RESERVED_WORDS = "ABSOLUTE,ACTION,ADD,AFTER,ALL,ALLOCATE,ALTER,AND,ANY,ARE,ARRAY,"
                + "AS,ASC,ASENSITIVE,ASSERTION,ASYMMETRIC,AT,ATOMIC,AUTHORIZATION,AVG,BEFORE,BEGIN,BETWEEN,BIGINT,BINARY,BIT,"
                + "BIT_LENGTH,BLOB,BOOLEAN,BOTH,BREADTH,BY,CALL,CALLED,CASCADE,CASCADED,CASE,CAST,CATALOG,CHAR,CHAR_LENGTH,"
                + "CHARACTER,CHARACTER_LENGTH,CHECK,CLOB,CLOSE,COALESCE,COLLATE,COLLATION,COLUMN,COMMIT,CONDITION,CONNECT,"
                + "CONNECTION,CONSTRAINT,CONSTRAINTS,CONSTRUCTOR,CONTAINS,CONTINUE,CONVERT,CORRESPONDING,COUNT,CREATE,CROSS,CUBE,"
                + "CURRENT,CURRENT_DATE,CURRENT_DEFAULT_TRANSFORM_GROUP,CURRENT_PATH,CURRENT_ROLE,CURRENT_TIME,CURRENT_TIMESTAMP,"
                + "CURRENT_TRANSFORM_GROUP_FOR_TYPE,CURRENT_USER,CURSOR,CYCLE,DATA,DATE,DAY,DEALLOCATE,DEC,DECIMAL,DECLARE,DEFAULT,"
                + "DEFERRABLE,DEFERRED,DELETE,DEPTH,DEREF,DESC,DESCRIBE,DESCRIPTOR,DETERMINISTIC,DIAGNOSTICS,DISCONNECT,DISTINCT,"
                + "DO,DOMAIN,DOUBLE,DROP,DYNAMIC,EACH,ELEMENT,ELSE,ELSEIF,END,EQUALS,ESCAPE,EXCEPT,EXCEPTION,EXEC,EXECUTE,EXISTS,"
                + "EXIT,EXTERNAL,EXTRACT,FALSE,FETCH,FILTER,FIRST,FLOAT,FOR,FOREIGN,FOUND,FREE,FROM,FULL,FUNCTION,GENERAL,GET,GLOBAL,"
                + "GO,GOTO,GRANT,GROUP,GROUPING,HANDLER,HAVING,HOLD,HOUR,IDENTITY,IF,IMMEDIATE,IN,INDICATOR,INITIALLY,INNER,INOUT,"
                + "INPUT,INSENSITIVE,INSERT,INT,INTEGER,INTERSECT,INTERVAL,INTO,IS,ISOLATION,ITERATE,JOIN,KEY,LANGUAGE,LARGE,LAST,"
                + "LATERAL,LEADING,LEAVE,LEFT,LEVEL,LIKE,LOCAL,LOCALTIME,LOCALTIMESTAMP,LOCATOR,LOOP,LOWER,MAP,MATCH,MAX,MEMBER,"
                + "MERGE,METHOD,MIN,MINUTE,MODIFIES,MODULE,MONTH,MULTISET,NAMES,NATIONAL,NATURAL,NCHAR,NCLOB,NEW,NEXT,NO,NONE,NOT,"
                + "NULL,NULLIF,NUMERIC,OBJECT,OCTET_LENGTH,OF,OLD,ON,ONLY,OPEN,OPTION,OR,ORDER,ORDINALITY,OUT,OUTER,OUTPUT,OVER,"
                + "OVERLAPS,PAD,PARAMETER,PARTIAL,PARTITION,PATH,POSITION,PRECISION,PREPARE,PRESERVE,PRIMARY,PRIOR,PRIVILEGES,"
                + "PROCEDURE,PUBLIC,RANGE,READ,READS,REAL,RECURSIVE,REF,REFERENCES,REFERENCING,RELATIVE,RELEASE,REPEAT,RESIGNAL,"
                + "RESTRICT,RESULT,RETURN,RETURNS,REVOKE,RIGHT,ROLE,ROLLBACK,ROLLUP,ROUTINE,ROW,ROWS,SAVEPOINT,SCHEMA,SCOPE,SCROLL,"
                + "SEARCH,SECOND,SECTION,SELECT,SENSITIVE,SESSION,SESSION_USER,SET,SETS,SIGNAL,SIMILAR,SIZE,SMALLINT,SOME,SPACE,"
                + "SPECIFIC,SPECIFICTYPE,SQL,SQLCODE,SQLERROR,SQLEXCEPTION,SQLSTATE,SQLWARNING,START,STATE,STATIC,SUBMULTISET,"
                + "SUBSTRING,SUM,SYMMETRIC,SYSTEM,SYSTEM_USER,TABLE,TABLESAMPLE,TEMPORARY,THEN,TIME,TIMESTAMP,TIMEZONE_HOUR,"
                + "TIMEZONE_MINUTE,TO,TRAILING,TRANSACTION,TRANSLATE,TRANSLATION,TREAT,TRIGGER,TRIM,TRUE,UNDER,UNDO,UNION,UNIQUE,"
                + "UNKNOWN,UNNEST,UNTIL,UPDATE,UPPER,USAGE,USER,USING,VALUE,VALUES,VARCHAR,VARYING,VIEW,WHEN,WHENEVER,WHERE,WHILE,"
                + "WINDOW,WITH,WITHIN,WITHOUT,WORK,WRITE,YEAR,ZONE";

        static {
            SEQUENCE_TABLE_NAME = System.getProperty("SEQUENCE_TABLE_NAME") == null ? "SEQ"
                    : System.getProperty("SEQUENCE_TABLE_NAME");
            SYSTEM_SCHEMAS.put(RiderDataSource.DBType.MSSQL, Collections.singleton("SYS"));
            SYSTEM_SCHEMAS.put(RiderDataSource.DBType.H2, Collections.singleton("INFORMATION_SCHEMA"));

            Collections.addAll(RESERVED_TABLE_NAMES, DEFAULT_SQL_RESERVED_WORDS.split(","));
            if (System.getProperty("RESERVED_TABLE_NAMES") != null) {
                Collections.addAll(RESERVED_TABLE_NAMES, System.getProperty("RESERVED_TABLE_NAMES").toUpperCase().split(","));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBUnitConfig that = (DBUnitConfig) o;
        return Objects.equals(executorId, that.executorId) &&
                Objects.equals(cacheConnection, that.cacheConnection) &&
                Objects.equals(cacheTableNames, that.cacheTableNames) &&
                Objects.equals(leakHunter, that.leakHunter) &&
                Objects.equals(mergeDataSets, that.mergeDataSets) &&
                Objects.equals(columnSensing, that.columnSensing) &&
                Objects.equals(raiseExceptionOnCleanUp, that.raiseExceptionOnCleanUp) &&
                Objects.equals(disableSequenceFiltering, that.disableSequenceFiltering) &&
                Objects.equals(alwaysCleanBefore, that.alwaysCleanBefore) &&
                Objects.equals(alwaysCleanAfter, that.alwaysCleanAfter) &&
                caseInsensitiveStrategy == that.caseInsensitiveStrategy &&
                Arrays.equals(disablePKCheckFor, that.disablePKCheckFor) &&
                mergingStrategy == that.mergingStrategy &&
                expectedDbType == that.expectedDbType &&
                Objects.equals(properties, that.properties) &&
                Objects.equals(connectionConfig, that.connectionConfig);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(executorId, properties);
        result = 31 * result;
        return result;
    }
}
