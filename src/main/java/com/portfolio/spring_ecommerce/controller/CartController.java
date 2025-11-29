package com.portfolio.spring_ecommerce.controller;

import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.service.CartService;
import com.portfolio.spring_ecommerce.repository.ProductRepository;
import com.portfolio.spring_ecommerce.util.GetAuthenticatedUser;
import com.portfolio.spring_ecommerce.repository.CartItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user/cart")
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final GetAuthenticatedUser getAuthenticatedUserUtil;

    public CartController(CartService cartService, ProductRepository productRepository, CartItemRepository cartItemRepository, GetAuthenticatedUser getAuthenticatedUserUtil) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.getAuthenticatedUserUtil = getAuthenticatedUserUtil;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> getCart() {
        User user = getAuthenticatedUserUtil.getAuthenticatedUser();
        if (user == null) return ResponseEntity.notFound().build();
        Cart cart = cartService.getOrCreateCart(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> addItem(
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
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> updateQuantity(
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
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> removeItem(
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
        return ResponseEntity.ok(cart);
    }
}