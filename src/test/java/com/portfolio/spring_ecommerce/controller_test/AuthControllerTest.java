package com.portfolio.spring_ecommerce.controller_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.spring_ecommerce.controller.AuthController;
import com.portfolio.spring_ecommerce.dto.AuthRequest;
import com.portfolio.spring_ecommerce.service.JwtBlacklistService;
import com.portfolio.spring_ecommerce.service.UserService;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @MockitoBean
    private JwtBlacklistService jwtBlacklistService; // JwtBlacklistServiceのモック

    @Autowired
    private ObjectMapper objectMapper; // JSON変換用

    @MockitoBean
    private UserRepository userRepository; // UserRepositoryのモック

    @MockitoBean
    private PasswordEncoder passwordEncoder; // PasswordEncoderのモック

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // 各テスト前にリポジトリをクリア
    }

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

    /**
     * 新規ユーザー登録のテスト
     */
    @Test
    void registerUser_whenNewUser_shouldReturnOk() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("newUser");
        request.setPassword("password");

        // モック: 新規ユーザーは存在しない
        Mockito.when(userRepository.findByUsername("newUser")).thenReturn(java.util.Optional.empty());
        // モック: save時にUserを返す
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("ユーザー登録が成功しました"));

        Mockito.verify(userRepository).save(Mockito.argThat(user -> user.getUsername().equals("newUser")));
    }
    
    /**
     * 既存のユーザー名で登録しようとした場合、400エラーが返却されることを検証
     */
    @Test
    void registerUser_whenUsernameExists_shouldReturnBadRequest() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setPassword(passwordEncoder.encode("password"));

        // モック: 既存ユーザーが存在する
        Mockito.when(userRepository.findByUsername("existingUser")).thenReturn(java.util.Optional.of(existingUser));

        AuthRequest request = new AuthRequest();
        request.setUsername("existingUser");
        request.setPassword("password");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ユーザー名は既に使用されています"));
    }

    /**
     * ログアウト時にトークンがブラックリストに追加されることを検証
     */
    @Test
    void logout_shouldAddTokenToBlacklist_whenTokenIsProvided() throws Exception {
        String token = "valid-jwt-token";

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ログアウトしました"));

        verify(jwtBlacklistService).addTokenToBlacklist(token);
    }

    /**
     * ログアウト時にAuthorizationヘッダーがない場合、エラーなく成功することを検証
     */
    @Test
    void logout_shouldSucceed_whenNoTokenIsProvided() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("ログアウトしました"));

        verify(jwtBlacklistService, never()).addTokenToBlacklist(any());
    }
}