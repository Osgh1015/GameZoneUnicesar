package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Applies the business rules of the warranty module: granting basic and
 * extended warranties when a sale is registered, and answering the
 * queries the store needs about coverage (warranty of a product inside
 * a sale, active warranties and warranties about to expire).
 *
 * The service never reads or writes files by itself: it delegates that
 * responsibility to the repository, respecting the layer separation
 * required by the workshop.
 */
public class WarrantyService {

    private final WarrantyRepository repository;
    private final SalePersistence salePersistence;
    private final ProductService productService;
    private final PersonService personService;
    private final List<Warranty> warranties;

    /**
     * Creates the service and resolves the references stored by the
     * warranty repository.
     *
     * @param repository      repository used to persist and load warranty records
     * @param salePersistence persistence used to rebuild stored sales
     * @param productService  service used to resolve product references
     * @param personService   service used to resolve client and seller references
     */
    public WarrantyService(
            WarrantyRepository repository,
            SalePersistence salePersistence,
            ProductService productService,
            PersonService personService) {

        this.repository = repository;
        this.salePersistence = salePersistence;
        this.productService = productService;
        this.personService = personService;

        this.warranties = loadWarranties();
    }

    /**
     * Loads warranty records and resolves their product and sale
     * references.
     *
     * @return warranties reconstructed from persistence
     */
    private List<Warranty> loadWarranties() {

        List<Warranty> result = new ArrayList<>();

        List<Sale> sales =
                salePersistence.loadAll(
                        productService,
                        personService
                );

        for (WarrantyRecord record : repository.loadAll()) {

            Product product =
                    productService.findById(
                            record.productId()
                    );

            Sale sale =
                    findSaleById(
                            sales,
                            record.saleId()
                    );

            if (product == null || sale == null) {
                continue;
            }

            Warranty warranty =
                    switch (record.type()) {

                        case "BASIC" ->
                                new BasicWarranty(
                                        record.id(),
                                        product,
                                        sale,
                                        record.startDate()
                                );

                        case "EXTENDED" ->
                                new ExtendedWarranty(
                                        record.id(),
                                        product,
                                        sale,
                                        record.startDate()
                                );

                        default -> null;
                    };

            if (warranty != null) {
                result.add(warranty);
            }
        }

        return result;
    }

    /**
     * Finds a sale by its identifier.
     *
     * @param sales  sales available in persistence
     * @param saleId identifier of the sale
     * @return matching sale, or null when it does not exist
     */
    private Sale findSaleById(
            List<Sale> sales,
            String saleId) {

        for (Sale sale : sales) {

            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }

        return null;
    }

    /**
     * Creates and persists the free factory warranty granted to a
     * product sold in a given sale.
     *
     * @param product   product covered by the warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the coverage begins
     * @return the basic warranty that was granted
     */
    public BasicWarranty assignBasicWarranty(
            Product product,
            Sale sale,
            LocalDate startDate) {

        BasicWarranty warranty =
                new BasicWarranty(
                        generateId(),
                        product,
                        sale,
                        startDate
                );

        warranties.add(warranty);

        repository.saveAll(warranties);

        return warranty;
    }

    /**
     * Creates and persists the optional extended warranty bought by the
     * client for a product sold in a given sale.
     *
     * @param product   product covered by the warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the coverage begins
     * @return the extended warranty that was granted
     */
    public ExtendedWarranty assignExtendedWarranty(
            Product product,
            Sale sale,
            LocalDate startDate) {

        ExtendedWarranty warranty =
                new ExtendedWarranty(
                        generateId(),
                        product,
                        sale,
                        startDate
                );

        warranties.add(warranty);

        repository.saveAll(warranties);

        return warranty;
    }

    /**
     * Finds the warranty associated with a product inside a specific
     * sale. The sale is part of the search because the same product can
     * be sold many times, and each sale generates its own coverage.
     *
     * @param productId identifier of the covered product
     * @param saleId    identifier of the sale
     * @return the matching warranty, or {@code null} when the product
     *         has no warranty in that sale
     */
    public Warranty findWarrantyByProduct(
            String productId,
            String saleId) {

        for (Warranty warranty : warranties) {

            if (warranty.getProduct().getId().equals(productId)
                    && warranty.getSale().getId().equals(saleId)) {

                return warranty;
            }
        }

        return null;
    }

    /**
     * Lists every warranty registered in the system.
     *
     * @return a copy of the list of warranties
     */
    public List<Warranty> listAllWarranties() {
        return new ArrayList<>(warranties);
    }

    /**
     * Lists the warranties whose coverage is active on the current date.
     *
     * @return the warranties in force today
     */
    public List<Warranty> listActiveWarranties() {

        LocalDate today = LocalDate.now();
        List<Warranty> result = new ArrayList<>();

        for (Warranty warranty : warranties) {

            if (warranty.isActive(today)) {
                result.add(warranty);
            }
        }

        return result;
    }

    /**
     * Lists the warranties that are still active today but expire
     * within the next given number of days.
     *
     * @param daysAhead number of days of anticipation
     * @return the warranties about to expire
     */
    public List<Warranty> listWarrantiesExpiringSoon(
            int daysAhead) {

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);

        List<Warranty> result = new ArrayList<>();

        for (Warranty warranty : warranties) {

            LocalDate endDate =
                    warranty.getEndDate();

            if (!endDate.isBefore(today)
                    && !endDate.isAfter(limit)) {

                result.add(warranty);
            }
        }

        return result;
    }

    /**
     * Calculates the additional warranty cost associated with a sale.
     *
     * @param saleId identifier of the sale
     * @return total additional warranty cost
     */
    public double calculateWarrantyCost(
            String saleId) {

        double total = 0.0;

        for (Warranty warranty : warranties) {

            if (warranty.getSale().getId().equals(saleId)) {
                total += warranty.getAdditionalCost();
            }
        }

        return total;
    }

    /**
     * Generates a unique identifier for a new warranty.
     *
     * @return the identifier of the warranty
     */
    private String generateId() {
        return "W-" + UUID.randomUUID();
    }
}