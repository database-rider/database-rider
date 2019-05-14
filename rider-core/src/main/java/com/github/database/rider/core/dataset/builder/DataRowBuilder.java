package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.util.DateUtils;
import org.dbunit.dataset.IDataSet;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import javax.persistence.metamodel.Attribute;
import java.util.Calendar;
import java.util.Date;

import static com.github.database.rider.core.dataset.builder.BuilderUtil.convertCase;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

public class DataRowBuilder extends BasicDataRowBuilder {

    private final DataSetBuilder dataSet;
    private boolean added;

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
            columnName = convertCase(column.getName(), config);
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

    /**
     * Starts creating rows for new table
     * @param tableName
     * @return
     */
    public DataRowBuilder table(String tableName) {
        saveCurrentRow(); //save current row  every time a new row is started
        DataRowBuilder dataRowBuilder = new DataRowBuilder(dataSet, tableName);
        dataSet.setCurrentRowBuilder(dataRowBuilder);
        return dataRowBuilder;
    }

    public DataRowBuilder row() {
        if(!columnNameToValue.isEmpty()) {
            saveCurrentRow();
            dataSet.getCurrentRowBuilder().setAdded(false);
            columnNameToValue.clear();
        }
        return dataSet.getCurrentRowBuilder();
    }


    private void saveCurrentRow() {
        added = true;
        dataSet.add(this);
    }

    /**
     * Just a shortcut to build method
     *
     */
    public IDataSet build() {
        return dataSet.build();
    }

    /**
     * indicates wheater current row was added to the dataset being build
     */
    protected boolean isAdded() {
        return added;
    }

    protected void setAdded(boolean added) {
        this.added = added;
    }
}
