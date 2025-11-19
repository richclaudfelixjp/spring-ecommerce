package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.dto.AuthRequest;
import com.portfolio.spring_ecommerce.dto.AuthResponse;
import com.portfolio.spring_ecommerce.service.UserService;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 認証（ログイン）を処理するコントローラー
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, 
                         UserService userService, 
                         JwtUtil jwtUtil,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ログインエンドポイント
     * ユーザー名とパスワードを受け取り、JWTトークンを返す
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // ユーザー名とパスワードで認証を試みる
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), 
                            authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // 認証失敗時は401エラーを返す
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("ユーザー名またはパスワードが正しくありません");
        }

        // 認証成功時、ユーザー情報を取得してJWTトークンを生成
        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // JWTトークンとユーザー名を返す
        return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername()));
    }

    /**
     * ユーザー登録エンドポイント
     * 新しいユーザーを登録する
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest registrationRequest) {
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            // ユーザー名が既に存在する場合は400エラーを返す
            return ResponseEntity.badRequest().body("ユーザー名は既に使用されています");
        }
        // 新しいユーザーを作成して保存
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        return ResponseEntity.ok("ユーザー登録が成功しました");
    }
}