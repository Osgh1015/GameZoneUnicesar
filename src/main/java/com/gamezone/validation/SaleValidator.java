package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;

import java.util.List;

/**
 * Validations of the sales module. It is called by the services before
 * registering or consulting a sale.
 * <p>
 * It only contains static methods, uses only model classes and exceptions,
 * and never calls services or repositories.
 */
public final class SaleValidator {

    /**
     * Prevents creating instances: this class only has static methods.
     */
    private SaleValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates the data needed to register a sale.
     *
     * @param client   client that buys
     * @param seller   seller that attends the sale
     * @param products items of the sale
     * @throws InvalidDataException  if the client or the seller are missing
     * @throws BusinessRuleException if the sale has no products
     */
    public static void validateSaleData(Client client, Seller seller, List<Product> products) {
        if (client == null) {
            throw new InvalidDataException("cliente", "es obligatorio para registrar la venta.");
        }
        if (seller == null) {
            throw new InvalidDataException("vendedor", "es obligatorio para registrar la venta.");
        }
        if (products == null || products.isEmpty()) {
            throw new BusinessRuleException("Una venta debe contener al menos un producto.");
        }
    }

    /**
     * Validates that a sale searched by its identifier exists.
     *
     * @param sale   result of the search, {@code null} if it was not found
     * @param saleId identifier that was searched
     * @throws ResourceNotFoundException if the sale is null
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("la venta", saleId);
        }
    }
}