package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.persistence.PromotionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the promotions module: registration and validation of
 * promotions, listing of all and currently valid promotions, and selection
 * of the best promotion for a sale (the one granting the highest monetary
 * discount; promotions are not cumulative).
 */
public class PromotionService {

    private final PromotionRepository repository;
    private final List<Promotion> promotions;

    /**
     * @param repository repository used to persist promotions
     */
    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
        this.promotions = repository.loadAll();
    }

    /**
     * Registers a percentage promotion.
     *
     * @param id         unique identifier
     * @param name       promotion name
     * @param startDate  first valid day
     * @param endDate    last valid day
     * @param percentage discount percentage (0-100)
     * @return the registered promotion
     * @throws IllegalArgumentException if any business rule is violated
     */
    public Promotion registerPercentageDiscount(String id, String name, LocalDate startDate,
                                                LocalDate endDate, double percentage) {
        validateCommon(id, name, startDate, endDate);
        validatePercentage(percentage);
        return save(new PercentageDiscount(id, name, startDate, endDate, percentage));
    }

    /**
     * Registers a category promotion.
     *
     * @param id             unique identifier
     * @param name           promotion name
     * @param startDate      first valid day
     * @param endDate        last valid day
     * @param percentage     discount percentage (0-100)
     * @param targetCategory "VIDEOGAME" or "CONSOLE"
     * @return the registered promotion
     * @throws IllegalArgumentException if any business rule is violated
     */
    public Promotion registerCategoryDiscount(String id, String name, LocalDate startDate,
                                              LocalDate endDate, double percentage,
                                              String targetCategory) {
        validateCommon(id, name, startDate, endDate);
        validatePercentage(percentage);
        if (targetCategory == null
                || !(targetCategory.equalsIgnoreCase(CategoryDiscount.VIDEOGAME)
                || targetCategory.equalsIgnoreCase(CategoryDiscount.CONSOLE))) {
            throw new IllegalArgumentException("La categoría debe ser VIDEOGAME o CONSOLE.");
        }
        return save(new CategoryDiscount(id, name, startDate, endDate, percentage,
                targetCategory.toUpperCase()));
    }

    /**
     * Registers a bulk purchase promotion.
     *
     * @param id          unique identifier
     * @param name        promotion name
     * @param startDate   first valid day
     * @param endDate     last valid day
     * @param minQuantity minimum number of products in the sale
     * @param percentage  discount percentage (0-100)
     * @return the registered promotion
     * @throws IllegalArgumentException if any business rule is violated
     */
    public Promotion registerBulkPurchaseDiscount(String id, String name, LocalDate startDate,
                                                  LocalDate endDate, int minQuantity,
                                                  double percentage) {
        validateCommon(id, name, startDate, endDate);
        validatePercentage(percentage);
        if (minQuantity < 1) {
            throw new IllegalArgumentException("La cantidad mínima debe ser al menos 1.");
        }
        return save(new BulkPurchaseDiscount(id, name, startDate, endDate, minQuantity, percentage));
    }

    /** @return every registered promotion, including expired and future ones */
    public List<Promotion> listAllPromotions() {
        return new ArrayList<>(promotions);
    }

    /** @return the promotions valid on the current date */
    public List<Promotion> listActivePromotions() {
        return listActivePromotions(LocalDate.now());
    }

    /**
     * Returns the promotions valid on the given date. The date rule itself
     * lives in {@link Promotion#isActive(LocalDate)}; this service only
     * decides which date to evaluate.
     *
     * @param date date to evaluate
     * @return the promotions valid on that date
     */
    public List<Promotion> listActivePromotions(LocalDate date) {
        List<Promotion> result = new ArrayList<>();
        for (Promotion promotion : promotions) {
            if (promotion.isActive(date)) {
                result.add(promotion);
            }
        }
        return result;
    }

    /**
     * Selects, among the promotions valid on the sale date, the one that
     * grants the highest monetary discount.
     *
     * @param sale sale to evaluate
     * @return the best promotion, or {@code null} if none applies or the
     *         maximum discount is zero
     */
    public Promotion findBestPromotionFor(Sale sale) {
        Promotion best = null;
        double bestDiscount = 0.0;
        for (Promotion promotion : listActivePromotions(sale.getDate())) {
            double discount = promotion.calculateDiscount(sale);
            if (discount > bestDiscount) {
                bestDiscount = discount;
                best = promotion;
            }
        }
        return best;
    }

    /**
     * @param id promotion identifier
     * @return the matching promotion, or {@code null} if not found
     */
    public Promotion findById(String id) {
        for (Promotion promotion : promotions) {
            if (promotion.getId().equals(id)) {
                return promotion;
            }
        }
        return null;
    }

    private void validateCommon(String id, String name, LocalDate startDate, LocalDate endDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador no puede estar vacío.");
        }
        if (findById(id) != null) {
            throw new IllegalArgumentException("Ya existe una promoción con ese identificador.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (name.contains(";")) {
            throw new IllegalArgumentException("El nombre no puede contener el carácter ';'.");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        }
    }

    private void validatePercentage(double percentage) {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    private Promotion save(Promotion promotion) {
        promotions.add(promotion);
        repository.saveAll(promotions);
        return promotion;
    }
}