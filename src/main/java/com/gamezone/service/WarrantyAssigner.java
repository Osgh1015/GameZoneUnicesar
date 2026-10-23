package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;

import java.time.LocalDate;

/**
 * Granting of the warranties of a sale. The sale registration only needs
 * these two operations; it does not depend on the queries, the listings or
 * the cancellation of the warranty service.
 */
public interface WarrantyAssigner {

    /**
     * Grants and stores the basic warranty of a product sold in a sale.
     *
     * @param product   covered product
     * @param sale      sale in which the product was bought
     * @param startDate first day of the coverage
     * @return the basic warranty granted
     */
    BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate);

    /**
     * Grants and stores the extended warranty bought for a product in a sale.
     *
     * @param product   covered product
     * @param sale      sale in which the product was bought
     * @param startDate first day of the coverage
     * @return the extended warranty granted
     */
    ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate);
}
