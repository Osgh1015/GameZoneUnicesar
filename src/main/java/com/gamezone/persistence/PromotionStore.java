package com.gamezone.persistence;

import com.gamezone.model.Promotion;

import java.util.List;

/**
 * Abstraction of the place where the promotions are stored. The service that
 * applies the rules of the module depends on this interface, not on a
 * concrete file format, so the storage can change without changing the
 * service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface PromotionStore {

    /**
     * Saves the full list of promotions, replacing what was stored before.
     *
     * @param promotions promotions to persist
     */
    void saveAll(List<Promotion> promotions);

    /**
     * Loads every stored promotion.
     *
     * @return the stored promotions, or an empty list if there are none yet
     */
    List<Promotion> loadAll();
}
