package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.replacers.DateTimeReplacer;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.ColumnSpec;
import org.dbunit.dataset.builder.DataRowBuilder;
import org.dbunit.dataset.builder.DataSetBuilder;
import org.dbunit.dataset.datatype.DataType;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import javax.persistence.metamodel.Attribute;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

/**
 * @author rmpestano
 */
public class RiderDataRowBuilder extends DataRowBuilder {

    private boolean uppercase;

    protected RiderDataRowBuilder(DataSetBuilder dataSet, String tableName) {
        super(dataSet, tableName);
        this.uppercase = false;
    }

    public RiderDataRowBuilder(DataSetBuilder dataSet, String tableName, boolean uppercase) {
        super(dataSet, tableName);
        this.uppercase = uppercase;
    }

    protected Column createColumn(String columnName) {
        Object value = columnNameToValue.get(columnName);
        DataType columnType = DataType.UNKNOWN;
        if (value instanceof Integer) {
            columnType = DataType.INTEGER;
        }
        if (value instanceof Long) {
            columnType = DataType.BIGINT_AUX_LONG;
        }
        if (value instanceof Double) {
            columnType = DataType.DOUBLE;
        }
        if (value instanceof Float) {
            columnType = DataType.FLOAT;
        }
        if (value instanceof Date) {
            columnType = DataType.DATE;
        }
        if (value instanceof Boolean) {
            columnType = DataType.BOOLEAN;
        }
        if (value instanceof BigDecimal) {
            columnType = DataType.DECIMAL;
        }
        if (value instanceof Number) {
            columnType = DataType.NUMERIC;
        }
        return new Column(columnName, columnType);
    }

    @Override
    public <T> RiderDataRowBuilder with(ColumnSpec<T> column, T value) {
        return with(column.name(), value);
    }

    @Override
    public RiderDataRowBuilder with(String columnName, Object value) {
        put(columnName, value);
        return this;
    }

    public RiderDataRowBuilder with(String columnName, Date value) {
        put(columnName, DateTimeReplacer.DBUNIT_DATE_FORMAT.format(value));
        return this;
    }

    public RiderDataRowBuilder with(String columnName, Calendar value) {
        put(columnName, DateTimeReplacer.DBUNIT_DATE_FORMAT.format(value.getTime()));
        return this;
    }

    public RiderDataRowBuilder with(Attribute column, Object value) {
        String columnName = getColumnNameFromMetaModel(column);
        return with(columnName, value);
    }

    private String getColumnNameFromMetaModel(Attribute column) {
        String columnName = null;
        try {
            if(isEclipseLinkOnClasspath()) {
                columnName = ((AttributeImpl) column).getMapping().getField().getName();
            } else if (isHibernateOnClasspath() && isEntityManagerActive()) {
                AbstractEntityPersister entityMetadata = (AbstractEntityPersister) em().getEntityManagerFactory().unwrap(SessionFactory.class).getClassMetadata(column.getJavaMember().getDeclaringClass());
                columnName = entityMetadata.getPropertyColumnNames(column.getName())[0];
            }
        }catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, String.format("Could not extract database column name from column %s and type %s",column.getName(), column.getDeclaringType().getJavaType().getName()));
        }
        if(columnName == null) {
            columnName = uppercase ? column.getName().toUpperCase() : column.getName();
        }
        return columnName;
    }

    public RiderDataRowBuilder with(Attribute column, Calendar value) {
        String columnName = getColumnNameFromMetaModel(column);
        return with(columnName, value);
    }

    public RiderDataRowBuilder with(Attribute column, Date value) {
        String columnName = getColumnNameFromMetaModel(column);
        return with(columnName, value);
    }

    protected void put(String columnName, Object value) {
        columnNameToValue.put(uppercase ? columnName.toLowerCase():columnName, value);
    }

    @Override
    public RiderDataSetBuilder add() throws DataSetException {
        return (RiderDataSetBuilder) super.add();
    }

    private boolean isHibernateOnClasspath() {
        return isOnClasspath("org.hibernate.Session");
    }

    private boolean isEclipseLinkOnClasspath() {
        return isOnClasspath("org.eclipse.persistence.mappings.DirectToFieldMapping");
    }
}
