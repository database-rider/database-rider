package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.replacers.DateTimeReplacer;
import com.github.database.rider.core.util.DateUtils;
import org.dbunit.dataset.DataSetException;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import javax.persistence.metamodel.Attribute;
import java.util.Calendar;
import java.util.Date;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

public class DataRowBuilder extends BasicDataRowBuilder {

    private final DataSetBuilder dataSet;

    protected DataRowBuilder(DataSetBuilder dataSet, String tableName) {
        super(tableName);
        this.dataSet = dataSet;
    }

    public <T> DataRowBuilder column(ColumnSpec column, T value) {
        super.column(column.name(), value);
        return this;
    }

    public DataRowBuilder column(String columnName, Date value) {
        put(columnName, DateUtils.format(value));
        return this;
    }

    public DataRowBuilder column(String columnName, Calendar value) {
        put(columnName, DateUtils.format(value.getTime()));
        return this;
    }

    public DataRowBuilder column(Attribute column, Object value) {
        String columnName = getColumnNameFromMetaModel(column);
        super.column(columnName, value);
        return this;
    }

    public DataRowBuilder column(String columnName, Object value) {
        super.column(columnName, value);
        return this;
    }

    private String getColumnNameFromMetaModel(Attribute column) {
        String columnName = null;
        try {
            if (isEclipseLinkOnClasspath()) {
                columnName = ((AttributeImpl) column).getMapping().getField().getName();
            } else if (isHibernateOnClasspath() && isEntityManagerActive()) {
                AbstractEntityPersister entityMetadata = (AbstractEntityPersister) em().getEntityManagerFactory().unwrap(SessionFactory.class).getClassMetadata(column.getJavaMember().getDeclaringClass());
                columnName = entityMetadata.getPropertyColumnNames(column.getName())[0];
            }
        } catch (Exception e) {
            LOGGER.error("Could not extract database column name from column {} and type {}", column.getName(), column.getDeclaringType().getJavaType().getName(), e);
        }
        if (columnName == null) {
            columnName = BuilderUtil.convertCase(column.getName(), config);
        }
        return columnName;
    }

    public DataRowBuilder column(Attribute column, Calendar value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    public DataRowBuilder column(Attribute column, Date value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    public DataSetBuilder add() throws DataSetException {
        dataSet.add(this);
        return dataSet;
    }

}
