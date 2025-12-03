package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.dto.OrderDTO;
import com.portfolio.spring_ecommerce.dto.UserOrdersResponseDTO;
import com.portfolio.spring_ecommerce.model.Order;
import com.portfolio.spring_ecommerce.model.User;
import com.portfolio.spring_ecommerce.service.OrderService;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注文関連の操作を行うコントローラー
 */
@RestController
@RequestMapping("/user/orders")
public class OrderController {

    private final OrderService orderService;
    private final GetAuthenticatedUser getAuthenticatedUserUtil;

    /**
     * 必要なサービスとユーティリティをDI（依存性注入）で受け取るコンストラクタ
     * @param orderService 注文サービス
     * @param getAuthenticatedUserUtil 認証済みユーザー取得ユーティリティ
     */
    public OrderController(OrderService orderService, GetAuthenticatedUser getAuthenticatedUserUtil) {
        this.orderService = orderService;
        this.getAuthenticatedUserUtil = getAuthenticatedUserUtil;
    }

    /**
     * 認証されたユーザーのカートから新しい注文を作成するエンドポイント
     * @return 作成された注文の情報を含むレスポンスエンティティ
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOrdersResponseDTO> createOrderFromCart() {
        try {
            User user = getAuthenticatedUserUtil.getAuthenticatedUser();
            Order order = orderService.createOrderFromCart(user);
            OrderDTO orderDTO = new OrderDTO(order);
            List<OrderDTO> orderDTOs = List.of(orderDTO);
            UserOrdersResponseDTO response = new UserOrdersResponseDTO(user.getUsername(), orderDTOs);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new IllegalArgumentException("カートが空のため、注文を作成できません。");
        }
    }

    /**
     * 認証されたユーザーのすべての注文を取得するエンドポイント
     * @return ユーザーの注文リストを含むレスポンスエンティティ
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOrdersResponseDTO> getOrdersForUser() {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        List<Order> orders = orderService.getOrdersForUser(user);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        
        UserOrdersResponseDTO response = new UserOrdersResponseDTO(user.getUsername(), orderDTOs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたIDの注文を取得するエンドポイント
     * @param orderId 取得する注文のID
     * @return 注文情報を含むレスポンスエンティティ、存在しない場合は404ステータス
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserOrdersResponseDTO> getOrderById(@PathVariable Long orderId) {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        return orderService.getOrderById(orderId)
                .filter(order -> order.getUser().getId().equals(user.getId()))
                .map(OrderDTO::new)
                .map(orderDTO -> {
                    List<OrderDTO> orderDTOs = List.of(orderDTO);
                    UserOrdersResponseDTO response = new UserOrdersResponseDTO(user.getUsername(), orderDTOs);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}