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
 * Provides the business operations related to warranties.
 *
 * It coordinates the creation, storage and consultation of warranty
 * objects while delegating file access to WarrantyRepository. The
 * repository only stores identifiers; this service resolves them into
 * {@link Sale} and {@link Product} objects when the warranties are loaded,
 * so the persistence layer does not depend on any service.
 *
 * Responsabilidad: Desarrollador 2 - Servicio de garantias.
 */
public class WarrantyService {

    private final WarrantyRepository repository;
    private final SalePersistence salePersistence;
    private final ProductService productService;
    private final PersonService personService;
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and loads the warranties already
     * stored, resolving the sale and product of each one from the
     * identifiers kept by the repository.
     *
     * @param repository      repository used to persist warranty records
     * @param salePersistence persistence used to rebuild the stored sales
     * @param productService  service used to resolve product references
     * @param personService   service used by SalePersistence to resolve
     *                        clients and sellers
     */
    public WarrantyService(WarrantyRepository repository,
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
     * Loads the warranty records and resolves their product and sale
     * references, rebuilding the concrete warranty type indicated by the
     * discriminator. Records whose product or sale no longer exist are
     * skipped.
     *
     * @return the warranties reconstructed from persistence
     */
    private List<Warranty> loadWarranties() {

        List<Warranty> result = new ArrayList<>();
        List<Sale> sales = salePersistence.loadAll(productService, personService);

        for (WarrantyRecord record : repository.loadAll()) {

            Product product = productService.findById(record.productId());
            Sale sale = findSaleById(sales, record.saleId());

            if (product == null || sale == null) {
                continue;
            }

            Warranty warranty = switch (record.type()) {
                case "BASIC" -> new BasicWarranty(record.id(), product, sale, record.startDate());
                case "EXTENDED" -> new ExtendedWarranty(record.id(), product, sale, record.startDate());
                default -> null;
            };

            if (warranty != null) {
                result.add(warranty);
            }
        }

        return result;
    }

    /**
     * Finds a sale by its identifier inside a list of sales.
     *
     * @param sales  sales rebuilt from persistence
     * @param saleId identifier of the sale
     * @return the matching sale, or {@code null} when it does not exist
     */
    private Sale findSaleById(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Creates and persists the basic warranty associated with a product
     * sold in a given sale.
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

        BasicWarranty warranty = new BasicWarranty(
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

        ExtendedWarranty warranty = new ExtendedWarranty(
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
     * Returns a copy of all warranties currently managed by the service.
     *
     * @return list containing all registered warranties
     */
    public List<Warranty> listAllWarranties() {
        return new ArrayList<>(warranties);
    }

    /**
     * Returns the warranties that are active on the current date.
     *
     * @return list containing all active warranties
     */
    public List<Warranty> listActiveWarranties() {

        LocalDate today = LocalDate.now();
        List<Warranty> activeWarranties = new ArrayList<>();

        for (Warranty warranty : warranties) {

            if (warranty.isActive(today)) {
                activeWarranties.add(warranty);
            }
        }

        return activeWarranties;
    }

    /**
     * Finds the warranty associated with a product inside a specific
     * sale. The sale is part of the search because the same product can
     * be sold many times, and each sale generates its own coverage. When
     * the product has both a basic and an extended warranty in the sale,
     * the extended one is returned, since it is the widest coverage.
     *
     * @param productId identifier of the covered product
     * @param saleId    identifier of the sale
     * @return the matching warranty, or {@code null} when the product has
     *         no warranty in that sale
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) {

        Warranty found = null;

        for (Warranty warranty : warranties) {

            boolean sameProduct = warranty.getProduct().getId().equals(productId);
            boolean sameSale = warranty.getSale().getId().equals(saleId);

            if (sameProduct && sameSale) {
                if (warranty instanceof ExtendedWarranty) {
                    return warranty;
                }
                found = warranty;
            }
        }

        return found;
    }

    /**
     * Returns the warranties that are still active today and whose end
     * date falls within the next {@code daysAhead} days (both limits
     * included).
     *
     * @param daysAhead number of days of anticipation
     * @return list of warranties about to expire
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) {

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);
        List<Warranty> expiringSoon = new ArrayList<>();

        for (Warranty warranty : warranties) {

            LocalDate endDate = warranty.getEndDate();

            if (!endDate.isBefore(today) && !endDate.isAfter(limit)) {
                expiringSoon.add(warranty);
            }
        }

        return expiringSoon;
    }

    /**
     * Cancels the warranties of a returned product in the indicated sale.
     * A returned console cannot keep a valid warranty, so its basic warranty
     * and, if it was bought, its extended warranty are removed. When the
     * same console appears several times in the sale, each call cancels the
     * warranties of one unit (one basic and one extended at most).
     *
     * @param productId identifier of the returned product
     * @param saleId    identifier of the original sale
     * @return the refundable cost of the cancelled warranties: zero for the
     *         basic warranty and the additional cost for the extended one
     */
    public double cancelWarranties(String productId, String saleId) {

        Warranty basicToCancel = null;
        Warranty extendedToCancel = null;

        for (Warranty warranty : warranties) {

            boolean sameProduct = warranty.getProduct().getId().equals(productId);
            boolean sameSale = warranty.getSale().getId().equals(saleId);

            if (!sameProduct || !sameSale) {
                continue;
            }

            if (warranty instanceof ExtendedWarranty) {
                if (extendedToCancel == null) {
                    extendedToCancel = warranty;
                }
            } else if (basicToCancel == null) {
                basicToCancel = warranty;
            }
        }

        double refundableCost = 0.0;

        if (basicToCancel != null) {
            warranties.remove(basicToCancel);
            refundableCost += basicToCancel.getAdditionalCost();
        }
        if (extendedToCancel != null) {
            warranties.remove(extendedToCancel);
            refundableCost += extendedToCancel.getAdditionalCost();
        }

        if (basicToCancel != null || extendedToCancel != null) {
            repository.saveAll(warranties);
        }

        return refundableCost;
    }

    /**
     * Calculates the total additional cost of all warranties associated
     * with a specific sale.
     *
     * Basic warranties do not add any cost, while extended warranties
     * contribute their additional cost.
     *
     * @param saleId identifier of the sale
     * @return total additional warranty cost for the sale
     */
    public double calculateWarrantyCost(String saleId) {

        double total = 0.0;

        for (Warranty warranty : warranties) {

            if (warranty.getSale().getId().equals(saleId)) {
                total += warranty.getAdditionalCost();
            }
        }

        return total;
    }

    /**
     * Generates a unique identifier for a warranty.
     *
     * @return generated warranty identifier
     */
    private String generateId() {
        return "W-" + UUID.randomUUID();
    }
}