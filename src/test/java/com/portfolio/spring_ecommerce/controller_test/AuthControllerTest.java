package com.portfolio.spring_ecommerce.controller_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.spring_ecommerce.controller.AuthController;
import com.portfolio.spring_ecommerce.dto.AuthRequest;
import com.portfolio.spring_ecommerce.service.UserService;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerの結合テストクラス
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // セキュリティフィルターを無効化してテスト
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // モックMVCを使用してコントローラのテストを実施

    @MockitoBean
    private AuthenticationManager authenticationManager; // AuthenticationManagerのモック

    @MockitoBean
    private UserService userService; // UserServiceのモック

    @MockitoBean
    private JwtUtil jwtUtil; // JwtUtilのモック

    @Autowired
    private ObjectMapper objectMapper; // JSON変換用

    /**
     * 正しい認証情報でログインした場合、JWTトークンが返却されることを検証
     */
    @Test
    void login_ReturnsJwtToken_WhenCredentialsAreValid() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        // UserDetailsのモック設定
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("testuser");
        Mockito.when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);
        Mockito.when(jwtUtil.generateToken(userDetails)).thenReturn("dummy-jwt-token");

        // /auth/loginエンドポイントへのリクエストとレスポンス検証
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /**
     * 誤った認証情報でログインした場合、401エラーとエラーメッセージが返却されることを検証
     */
    @Test
    void login_ReturnsUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("wronguser");
        request.setPassword("wrongpass");

        // 認証失敗時の例外をスローするようモック設定
        Mockito.doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // /auth/loginエンドポイントへのリクエストとレスポンス検証
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("ユーザー名またはパスワードが正しくありません"));
    }
}