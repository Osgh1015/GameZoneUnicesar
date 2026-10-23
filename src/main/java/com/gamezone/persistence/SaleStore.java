package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Sale;

import java.util.List;

/**
 * Abstraction of the place where the sales are stored. The sales module of
 * the service layer depends on this interface, not on a concrete file
 * format, so the storage can change without changing the services.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface SaleStore {

    /**
     * Saves the full list of sales, replacing what was stored before.
     *
     * @param sales sales to persist
     * @throws PersistenceException if the sales cannot be written
     */
    void saveAll(List<Sale> sales);

    /**
     * Loads the references of every stored sale.
     *
     * @return the stored sale records, or an empty list if there are none yet
     * @throws PersistenceException if the sales cannot be read
     */
    List<SalePersistence.SaleRecord> loadAll();

    /**
     * Builds the exception that reports a stored record that cannot be
     * rebuilt, naming where it is stored.
     *
     * @param lineNumber position of the record in the storage
     * @param cause      error found while rebuilding the record
     * @return the exception to throw
     */
    PersistenceException damagedLine(int lineNumber, RuntimeException cause);
}
