package com.gamezone.model;

import java.time.LocalDate;

/**
 * The kinds of warranty the store grants. Each kind knows how to build its
 * warranty and whether it is the preferred coverage when a product has more
 * than one warranty in a sale. A new kind of warranty is added here and as
 * a new subclass of {@link Warranty}; the services and the repository do
 * not change.
 */
public enum WarrantyKind {

    /** Free factory warranty of six months. */
    BASIC {
        @Override
        public Warranty create(String id, Product product, Sale sale, LocalDate startDate) {
            return new BasicWarranty(id, product, sale, startDate);
        }

        @Override
        public boolean isPreferredCoverage() {
            return false;
        }
    },

    /** Optional extended warranty of twelve months. */
    EXTENDED {
        @Override
        public Warranty create(String id, Product product, Sale sale, LocalDate startDate) {
            return new ExtendedWarranty(id, product, sale, startDate);
        }

        @Override
        public boolean isPreferredCoverage() {
            return true;
        }
    };

    /**
     * Builds a warranty of this kind.
     *
     * @param id        identifier of the warranty
     * @param product   covered product
     * @param sale      sale in which the product was bought
     * @param startDate first day of the coverage
     * @return the new warranty
     */
    public abstract Warranty create(String id, Product product, Sale sale, LocalDate startDate);

    /**
     * Tells whether this kind is the coverage reported when a product has
     * warranties of several kinds in the same sale.
     *
     * @return true for the widest coverage
     */
    public abstract boolean isPreferredCoverage();
}
