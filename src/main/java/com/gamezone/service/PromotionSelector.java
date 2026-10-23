package com.gamezone.service;

import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;

/**
 * Selection of the promotion applied to a sale. The sale registration only
 * needs this operation; it does not depend on the registration or the
 * listings of the promotions service.
 */
public interface PromotionSelector {

    /**
     * Selects, among the promotions valid on the sale date, the one that
     * grants the highest discount.
     *
     * @param sale sale to evaluate
     * @return the best promotion, or {@code null} if none applies
     */
    Promotion findBestPromotionFor(Sale sale);
}
