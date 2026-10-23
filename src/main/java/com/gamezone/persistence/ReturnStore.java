package com.gamezone.persistence;

import com.gamezone.model.Return;

import java.util.List;

/**
 * Abstraction of the place where the returns are stored. The service that
 * applies the rules of the module depends on this interface, not on a
 * concrete file format, so the storage can change without changing the
 * service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface ReturnStore {

    /**
     * Saves the full list of returns, replacing what was stored before.
     *
     * @param returns returns to persist
     */
    void saveAll(List<Return> returns);

    /**
     * Loads the references of every stored return.
     *
     * @return the stored return records, or an empty list if there are none yet
     */
    List<ReturnRepository.ReturnRecord> loadAll();
}
