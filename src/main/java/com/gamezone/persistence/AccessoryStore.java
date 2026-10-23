package com.gamezone.persistence;

import com.gamezone.model.Accessory;

import java.util.List;

/**
 * Abstraction of the place where the accessories are stored. The service that
 * applies the rules of the module depends on this interface, not on a
 * concrete file format, so the storage can change without changing the
 * service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface AccessoryStore {

    /**
     * Saves the full list of accessories, replacing what was stored before.
     *
     * @param accessories accessories to persist
     */
    void saveAll(List<Accessory> accessories);

    /**
     * Loads every stored accessory.
     *
     * @return the stored accessories, or an empty list if there are none yet
     */
    List<Accessory> loadAll();
}
