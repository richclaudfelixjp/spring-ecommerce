package com.portfolio.spring_ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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