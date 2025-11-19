package com.portfolio.spring_ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true",
        "ADMIN_PASSWORD=test-admin",
        "USER_PASSWORD=test-user"
})
class SpringEcommerceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // コンテキストが正常にロードされることを確認
        assertNotNull(applicationContext);
    }

    @Test
    void mainApplicationBeanExists() {
        // SpringEcommerceApplicationのBeanが存在することを確認
        assertTrue(applicationContext.containsBean("springEcommerceApplication"));
    }

    @Test
    void dataSourceBeanExists() {
        // DataSourceのBeanが存在することを確認（H2データベース接続）
        assertTrue(applicationContext.containsBean("dataSource"));
    }

    @Test
    void entityManagerFactoryBeanExists() {
        // EntityManagerFactoryのBeanが存在することを確認
        assertTrue(applicationContext.containsBean("entityManagerFactory"));
    }

    @Test
    void transactionManagerBeanExists() {
        // TransactionManagerのBeanが存在することを確認
        assertTrue(applicationContext.containsBean("transactionManager"));
    }
}