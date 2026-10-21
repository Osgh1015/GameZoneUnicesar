package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

/**
 * Validations of the products module. It is called by the services before
 * running any business operation on products. Accessories are products too,
 * so these validations also apply to them.
 * <p>
 * It only contains static methods, uses only model classes and exceptions,
 * and never calls services or repositories.
 */
public final class ProductValidator {

    /**
     * Prevents creating instances: this class only has static methods.
     */
    private ProductValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates the basic data of a product before registering it.
     *
     * @param id    identifier of the product
     * @param title title of the product
     * @param price price of the product
     * @param stock quantity available in the inventory
     * @throws InvalidDataException if the id or the title are empty, the
     *                              price is not positive or the stock is negative
     */
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "no puede estar vacío.");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidDataException("título", "no puede estar vacío.");
        }
        if (price <= 0) {
            throw new InvalidDataException("precio", "debe ser mayor que cero.");
        }
        if (stock < 0) {
            throw new InvalidDataException("stock", "no puede ser negativo.");
        }
    }

    /**
     * Validates that a product has enough stock for the requested quantity.
     *
     * @param product           product to check; it must exist (see
     *                          {@link #validateProductExists(Product, String)})
     * @param requestedQuantity quantity that is requested
     * @throws InvalidDataException  if the product is null or the requested
     *                               quantity is not positive
     * @throws BusinessRuleException if the stock is not enough
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (product == null) {
            throw new InvalidDataException("producto", "es obligatorio para validar el stock.");
        }
        if (requestedQuantity <= 0) {
            throw new InvalidDataException("cantidad", "debe ser mayor que cero.");
        }
        if (product.getQuantity() < requestedQuantity) {
            throw new BusinessRuleException("Stock insuficiente para '" + product.getTitle()
                    + "' (" + product.getId() + "): disponible " + product.getQuantity()
                    + ", solicitado " + requestedQuantity + ".");
        }
    }

    /**
     * Validates that a product searched by its identifier exists.
     *
     * @param product   result of the search, {@code null} if it was not found
     * @param productId identifier that was searched
     * @throws ResourceNotFoundException if the product is null
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("el producto", productId);
        }
    }
}