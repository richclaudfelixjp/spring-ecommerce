package com.portfolio.spring_ecommerce.service;

import com.portfolio.spring_ecommerce.model.*;
import com.portfolio.spring_ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * カートに関するビジネスロジックを処理するサービスクラス。
 * 商品の追加・削除・数量変更などの操作を提供する。
 */
@Service
public class CartService {

    /**
     * カートリポジトリへの参照。
     */
    private final CartRepository cartRepository;

    /**
     * カートアイテムリポジトリへの参照。
     */
    private final CartItemRepository cartItemRepository;

    /**
     * CartServiceのコンストラクタ。
     * Springの依存性注入によりリポジトリのインスタンスが注入される。
     * @param cartRepository カートリポジトリ
     * @param cartItemRepository カートアイテムリポジトリ
     */
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * ユーザーに紐づくカートを取得する。存在しない場合は新規作成する。
     * @param user 対象ユーザー
     * @return ユーザーのカート
     */
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user, new java.util.ArrayList<>())));
    }

    /**
     * カートに商品を追加する。
     * @param user 対象ユーザー
     * @param product 追加する商品
     * @param quantity 追加する数量
     * @return 更新後のカート
     */
    @Transactional
    public Cart addItemToCart(User user, Product product, int quantity) {
        if (product.getStatus() == null || !product.getStatus()) {
            throw new IllegalArgumentException("商品は購入できません。");
        }
        Cart cart = getOrCreateCart(user);
        CartItem item = new CartItem(product, quantity);
        cart.getItems().add(item);
        cartItemRepository.save(item);
        return cartRepository.save(cart);
    }

    /**
     * カートから商品を削除する。
     * @param user 対象ユーザー
     * @param cartItemId 削除するカートアイテムID
     * @return 更新後のカート
     */
    @Transactional
    public Cart removeItemFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.deleteById(cartItemId);
        return cartRepository.save(cart);
    }

    /**
     * カート内商品の数量を変更する。
     * @param user 対象ユーザー
     * @param cartItemId 変更するカートアイテムID
     * @param newQuantity 新しい数量
     * @return 更新後のカート
     */
    @Transactional
    public Cart updateCartItemQuantity(User user, Long cartItemId, int newQuantity) {
        Cart cart = getOrCreateCart(user);
        for (CartItem item : cart.getItems()) {
            if (item.getId().equals(cartItemId)) {
                item.setQuantity(newQuantity);
                cartItemRepository.save(item);
                break;
            }
        }
        return cartRepository.save(cart);
    }
}