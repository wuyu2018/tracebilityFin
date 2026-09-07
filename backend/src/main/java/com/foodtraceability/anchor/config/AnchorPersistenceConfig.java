package com.foodtraceability.anchor.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.foodtraceability.anchor.repository",
    entityManagerFactoryRef = "anchorEntityManagerFactory",
    transactionManagerRef = "anchorTransactionManager"
)
public class AnchorPersistenceConfig {

    @Value("${spring.datasource.anchor.url}")
    private String url;

    @Value("${spring.datasource.anchor.username}")
    private String username;

    @Value("${spring.datasource.anchor.password}")
    private String password;

    @Value("${spring.datasource.anchor.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Value("${spring.jpa.anchor.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Bean(name = "anchorDataSource")
    public DataSource anchorDataSource() {
        HikariDataSource ds = DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .driverClassName(driverClassName)
            .url(url)
            .username(username)
            .password(password)
            .build();
        ds.setConnectionTimeout(10000);
        ds.setMaximumPoolSize(3);
        ds.setMinimumIdle(1);
        ds.setInitializationFailTimeout(-1L);
        return ds;
    }

    @Bean(name = "anchorEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean anchorEntityManagerFactory(
            @Qualifier("anchorDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.foodtraceability.anchor.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        props.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        props.setProperty("hibernate.physical_naming_strategy",
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        em.setJpaProperties(props);
        em.setPersistenceUnitName("anchor-persistence-unit");
        return em;
    }

    @Bean(name = "anchorTransactionManager")
    public PlatformTransactionManager anchorTransactionManager(
            @Qualifier("anchorEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
