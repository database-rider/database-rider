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

public class RowBuilder extends BasicRowBuilder {

    private final TableBuilder tableBuilder;
    private boolean added;

    protected RowBuilder(TableBuilder tableBuilder, String tableName) {
        super(tableName);
        this.tableBuilder = tableBuilder;
    }

    /**
     * Adds a column to current row
     * @param column
     * @param value
     */
    public <T> RowBuilder column(ColumnSpec column, T value) {
        super.column(column.name(), value);
        return this;
    }

    /**
     * Adds a date column to current row
     * The date will be converted to dbunit default format
     * @param columnName
     * @param value
     */
    public RowBuilder column(String columnName, Date value) {
        put(columnName, DateUtils.format(value));
        return this;
    }

    /**
     * Adds a calendar column to current row
     * The calendar will be converted to dbunit default format
     * @param columnName
     * @param value
     */
    public RowBuilder column(String columnName, Calendar value) {
        put(columnName, DateUtils.format(value.getTime()));
        return this;
    }

    /**
     * Adds a column to current row based on JPA metamodel
     * @param column JPA metamodel column
     * @param value
     */
    public RowBuilder column(Attribute column, Object value) {
        String columnName = getColumnNameFromMetaModel(column);
        super.column(columnName, value);
        return this;
    }

    /**
     * Adds a column to current row
     * @param columnName
     * @param value
     */
    public RowBuilder column(String columnName, Object value) {
        super.column(columnName, value);
        return this;
    }

    /**
     * Adds a calendar column to current row based on JPA metamodel
     * The calendar will be converted to dbunit default format
     * @param column JPA metamodel column
     * @param value
     */
    public RowBuilder column(Attribute column, Calendar value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    /**
     * Adds a date column to current row based on JPA metamodel
     * The date will be converted to dbunit default format
     * @param column JPA metamodel column
     * @param value
     */
    public RowBuilder column(Attribute column, Date value) {
        String columnName = getColumnNameFromMetaModel(column);
        return column(columnName, value);
    }

    /**
     * starts a new row for current table
     */
    public RowBuilder row() {
        return tableBuilder.row();
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


    /**
     * Starts creating rows for a new table
     * @param tableName
     */
    public TableBuilder table(String tableName) {
        tableBuilder.saveCurrentRow(); //save current row  every time a new row is started
        return tableBuilder.getDataSetBuilder().table(tableName);
    }

    /**
     * Creates a dbunit dataset based on current builder
     */
    public IDataSet build() {
        return tableBuilder.getDataSetBuilder().build();
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
