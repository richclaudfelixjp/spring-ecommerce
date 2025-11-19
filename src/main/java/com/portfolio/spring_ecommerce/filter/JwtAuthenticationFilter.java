package com.portfolio.spring_ecommerce.filter;

import com.portfolio.spring_ecommerce.service.UserService;
import com.portfolio.spring_ecommerce.util.JwtUtil;
import com.portfolio.spring_ecommerce.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * リクエストごとにJWTトークンを検証し、認証情報をセットするフィルター
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final JwtBlacklistService jwtBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService, JwtBlacklistService jwtBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // リクエストヘッダーからAuthorizationヘッダーを取得
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Authorizationヘッダーが"Bearer "で始まる場合、JWTトークンを抽出
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            if (jwtBlacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWTトークンがブラックリストされました");
                return;
            }
            username = jwtUtil.extractUsername(jwt);
        }

        // ユーザー名が取得でき、かつまだ認証されていない場合
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // DBからユーザー情報を取得
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            // JWTトークンが有効な場合、認証情報をセット
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // 次のフィルターへ処理を渡す
        filterChain.doFilter(request, response);
    }
}