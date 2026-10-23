package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Seller;

/**
 * Read-only lookup of the people that take part in a sale. The sales module
 * only needs to find a client and a seller by id; it does not depend on the
 * registration or the listings of the people service.
 */
public interface PersonLookup {

    /**
     * Looks up a client by its identification.
     *
     * @param id the client id
     * @return the client, or {@code null} if not found
     */
    Client findClientById(String id);

    /**
     * Looks up a seller by its identification.
     *
     * @param id the seller id
     * @return the seller, or {@code null} if not found
     */
    Seller findSellerById(String id);
}
