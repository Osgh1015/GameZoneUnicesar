package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Applies the business rules of the products module.
 */
public class ProductService {

    private final ProductRepository repository;
    private final List<Product> products;

    public ProductService() {
        this.repository = new ProductRepository();
        this.products = repository.load();
    }

    public void registerVideoGame(VideoGame videoGame) {
        products.add(videoGame);
        repository.save(products);
    }

    public void registerConsole(Console console) {
        products.add(console);
        repository.save(products);
    }

    public List<Product> listProducts() {
        return Collections.unmodifiableList(products);
    }

    public boolean hasEnoughStock(String productId, int requestedQuantity) {
        Product product = findById(productId);
        return product != null && product.getQuantity() >= requestedQuantity;
    }

    public void decreaseStock(String productId, int amount) {
        Product product = findById(productId);
        if (product == null) {
            throw new NoSuchElementException("Product not found: " + productId);
        }
        if (product.getQuantity() < amount) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        product.setQuantity(product.getQuantity() - amount);
        repository.save(products);
    }

    public Product findById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) return product;
        }
        return null;
    }

    public void reduceStock(String productId, int amount) {
        decreaseStock(productId, amount);
    }

    public List<Product> listAll() {
        return listProducts();
    }
}