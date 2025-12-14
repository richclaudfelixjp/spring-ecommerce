package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.model.enums.OrderStatus;
import com.portfolio.spring_ecommerce.repository.CartRepository;
import com.portfolio.spring_ecommerce.repository.OrderRepository;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 注文に関するビジネスロジックを処理するサービスクラス。
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    /**
     * OrderServiceのコンストラクタ。
     * @param orderRepository 注文リポジトリ
     * @param cartRepository カートリポジトリ
     * @param productRepository 商品リポジトリ
     * @param cartService カートサービス
     */
    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    /**
     * ユーザーのカートから注文を作成する。
     * 在庫を確認し、注文を作成後、在庫を減らし、カートを空にする。
     * @param user 注文を作成するユーザー
     * @return 作成された注文
     * @throws IllegalStateException カートが空の場合や在庫が不足している場合
     */
    @Transactional
    public Order createOrderFromCart(User user) {
        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("カートが空のため、注文を作成できません。");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (product.getUnitsInStock() < quantity) {
                throw new IllegalStateException("在庫が不足しています: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getUnitPrice().doubleValue());
            order.addOrderItem(orderItem);

            product.setUnitsInStock(product.getUnitsInStock() - quantity);
            productRepository.save(product);

            totalAmount += orderItem.getPrice() * quantity;
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    /**
     * 特定のユーザーの注文履歴を取得する。
     * @param user ユーザー
     * @return 注文のリスト
     */
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }

    /**
     * 注文IDで注文を取得する。
     * @param orderId 注文ID
     * @return 注文（Optional）
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * PaymentIntent IDで注文を検索する
     */
    public Optional<Order> findByPaymentIntentId(String paymentIntentId) {
        return orderRepository.findByPaymentIntentId(paymentIntentId);
    }

    /**
     * 支払い成功時に注文のステータスを更新する
     */
    @Transactional
    public Order markOrderAsPaid(String paymentIntentId) {
        Order order = findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new IllegalStateException(
                "PaymentIntent ID: " + paymentIntentId + " の注文が見つかりません"));
        
        order.setStatus(OrderStatus.PAID);
        return orderRepository.save(order);
    }

    /**
     * 支払い失敗時に在庫を復元し注文をキャンセルする
     */
    @Transactional
    public void cancelOrderAndRestoreInventory(String paymentIntentId) {
        Order order = findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new IllegalStateException(
                "PaymentIntent ID: " + paymentIntentId + " の注文が見つかりません"));
        
        // 在庫を復元
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setUnitsInStock(product.getUnitsInStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    /**
     * 注文を保存する
     */
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }    
}