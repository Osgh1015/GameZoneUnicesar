package com.gamezone.persistence;

import com.gamezone.model.Warranty;

import java.util.List;

/**
 * Abstraction of the place where the warranties are stored. The service that
 * applies the rules of the module depends on this interface, not on a
 * concrete file format, so the storage can change without changing the
 * service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface WarrantyStore {

    /**
     * Saves the full list of warranties, replacing what was stored before.
     *
     * @param warranties warranties to persist
     */
    void saveAll(List<Warranty> warranties);

    /**
     * Loads the references of every stored warranty.
     *
     * @return the stored warranty records, or an empty list if there are none yet
     */
    List<WarrantyRepository.WarrantyRecord> loadAll();
}
