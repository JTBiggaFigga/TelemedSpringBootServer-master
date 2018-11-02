package com.mwellness.mcare.jdbcTemplates;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


public class MCareJdbcTemplate extends JdbcTemplate {
    public DriverManagerDataSource dataSource;
    public PlatformTransactionManager transactionManager;

    public void setDatasource(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}


