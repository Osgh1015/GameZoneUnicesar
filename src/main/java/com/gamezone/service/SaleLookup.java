package com.gamezone.service;

import com.gamezone.model.Sale;

import java.util.List;

/**
 * Read-only view of the registered sales. The returns module only needs to
 * find a sale and read the history; depending on this interface instead of
 * the whole sales service keeps it away from the registration of sales and
 * from every other operation it does not use.
 */
public interface SaleLookup {

    /**
     * Looks up a sale by its identifier.
     *
     * @param saleId identifier of the sale
     * @return the sale, or {@code null} if it does not exist
     */
    Sale findById(String saleId);

    /**
     * Returns the full history of registered sales.
     *
     * @return a copy of the list of sales
     */
    List<Sale> getSalesHistory();
}
