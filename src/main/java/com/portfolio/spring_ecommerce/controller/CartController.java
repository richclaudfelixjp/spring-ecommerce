package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.dto.CartDTO;
import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.service.CartService;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import com.portfolio.spring_ecommerce.repository.CartItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * カート関連の操作を行うコントローラー
 */
@RestController
@RequestMapping("/user/cart")
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final GetAuthenticatedUser getAuthenticatedUserUtil;

    /**
     * 必要なサービスとリポジトリをDI（依存性注入）で受け取るコンストラクタ
     * @param cartService カートサービス
     * @param productRepository 商品リポジトリ
     * @param cartItemRepository カートアイテムリポジトリ
     * @param getAuthenticatedUserUtil 認証済みユーザー取得ユーティリティ
     */
    public CartController(CartService cartService, ProductRepository productRepository, CartItemRepository cartItemRepository, GetAuthenticatedUser getAuthenticatedUserUtil) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.getAuthenticatedUserUtil = getAuthenticatedUserUtil;
    }

    /**
     * 認証されたユーザーのカート情報を取得するエンドポイント
     * @return カート情報を含むResponseEntity
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')") // USERロールのみアクセス可能
    public ResponseEntity<CartDTO> getCart() {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        if (user == null) return ResponseEntity.notFound().build();
        Cart cart = cartService.getOrCreateCart(user);
        CartDTO cartDTO = new CartDTO(user.getUsername(), cart.getItems());
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * カートに商品を追加するエンドポイント
     * @param productId 追加する商品のID
     * @param quantity 追加する数量
     * @return 更新されたカート情報を含むResponseEntity
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')") // USERロールのみアクセス可能
    public ResponseEntity<CartDTO> addItem(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        Product product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Cart cart = cartService.getOrCreateCart(user);
        boolean alreadyExists = cart.getItems().stream()
            .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (alreadyExists) {
            throw new IllegalArgumentException("この商品は既にカートに追加されています。");
        }

        if (!product.getStatus()) {
            throw new IllegalArgumentException("商品はカートに追加できません。");
        } else if (product.getUnitsInStock() < quantity) {
            throw new IllegalArgumentException("在庫が不足しています。");
        }

        cart = cartService.addItemToCart(user, product, quantity);
        CartDTO cartDTO = new CartDTO(user.getUsername(), cart.getItems());
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * カート内のアイテム数量を更新するエンドポイント
     * @param cartItemId 更新するカートアイテムのID
     * @param quantity 新しい数量
     * @return 更新されたカート情報を含むResponseEntity
     */
    @PutMapping("/update/{cartItemId}")
    @PreAuthorize("hasRole('USER')") // USERロールのみアクセス可能
    public ResponseEntity<CartDTO> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        if (user == null) return ResponseEntity.badRequest().build();
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElse(null);

        if (quantity <= 0) {
            throw new IllegalArgumentException("数量は1以上でなければなりません。");
        }

        if (cartItem.getProduct().getUnitsInStock() < quantity) {
            throw new IllegalArgumentException("在庫が不足しています。");
        }

        Cart cart = cartService.updateCartItemQuantity(user, cartItemId, quantity);
        CartDTO cartDTO = new CartDTO(user.getUsername(), cart.getItems());
        return ResponseEntity.ok(cartDTO);
    }

    /**
     * カートからアイテムを削除するエンドポイント
     * @param cartItemId 削除するカートアイテムのID
     * @return 更新されたカート情報を含むResponseEntity
     */
    @DeleteMapping("/remove/{cartItemId}")
    @PreAuthorize("hasRole('USER')") // USERロールのみアクセス可能
    public ResponseEntity<CartDTO> removeItem(
            @PathVariable Long cartItemId) {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        if (user == null) return ResponseEntity.badRequest().build();

        Cart cart = cartService.getOrCreateCart(user);
        boolean existsInUserCart = cart.getItems().stream()
            .anyMatch(item -> item.getId().equals(cartItemId));
        if (!existsInUserCart) {
            throw new IllegalArgumentException("このカートアイテムはあなたのカートに存在しません。");
        }

        cart = cartService.removeItemFromCart(user, cartItemId);
        CartDTO cartDTO = new CartDTO(user.getUsername(), cart.getItems());
        return ResponseEntity.ok(cartDTO);
    }
}