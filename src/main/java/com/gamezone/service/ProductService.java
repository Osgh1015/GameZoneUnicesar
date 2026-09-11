package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;

import java.util.Collections;
import java.util.List;

/**
 * Applies the business rules of the products module.
 */
public class ProductService {

    private final ProductRepository repository;
    private final List<Product> products;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
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
}   