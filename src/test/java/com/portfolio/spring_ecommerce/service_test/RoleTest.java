package com.portfolio.spring_ecommerce.service_test;

import com.portfolio.spring_ecommerce.model.User;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Userエンティティのロール管理に関する単体テストクラス。
 * ユーザーに複数ロールが正しく割り当てられることを検証する。
 */
class RoleTest {

    /**
     * ユーザーにロールを割り当てた際、正しく保持されることを検証するテスト。
     */
    @Test
    void testRolesAssignment() {

        User user = new User();
        user.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
        assertTrue(user.getRoles().contains("ROLE_USER"));
        assertTrue(user.getRoles().contains("ROLE_ADMIN"));
    }
}