package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Seller;

import java.util.List;

/**
 * Abstraction of the place where the clients and the sellers are stored. The service that
 * applies the rules of the module depends on this interface, not on a
 * concrete file format, so the storage can change without changing the
 * service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface PersonStore {

    /**
     * Saves the full list of clients, replacing what was stored before.
     *
     * @param clients clients to persist
     * @throws PersistenceException if the clients cannot be written
     */
    void saveClients(List<Client> clients);

    /**
     * Loads every stored client.
     *
     * @return the stored clients, or an empty list if there are none yet
     * @throws PersistenceException if the clients cannot be read
     */
    List<Client> loadClients();

    /**
     * Loads every stored seller.
     *
     * @return the stored sellers, or an empty list if there are none yet
     * @throws PersistenceException if the sellers cannot be read
     */
    List<Seller> loadVendors();
}
