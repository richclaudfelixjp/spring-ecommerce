package com.portfolio.spring_ecommerce.controller_test;

import com.portfolio.spring_ecommerce.controller.AdminController;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import com.portfolio.spring_ecommerce.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminControllerの結合テストクラス
 */
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // モックMVCを使用してコントローラのテストを実施

    @MockitoBean
    private JwtUtil jwtUtil; // JwtUtilのモック

    @MockitoBean
    private UserService userService; // UserServiceのモック

    /**
     * 管理者ダッシュボードへのアクセスが成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN") // 管理者権限で認証
    void adminDashboard_ReturnsSuccessMessage() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string("管理者ダッシュボード - アクセス成功"));
    }

    /**
     * ユーザー管理画面へのアクセスが成功することを検証
     */
    @Test
    @WithMockUser(roles = "ADMIN") // 管理者権限で認証
    void manageUsers_ReturnsUserManagementMessage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().string("ユーザー管理画面"));
    }
}