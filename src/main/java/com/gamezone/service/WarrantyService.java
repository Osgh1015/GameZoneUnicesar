package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.model.WarrantyKind;
import com.gamezone.persistence.WarrantyStore;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
public class WarrantyService implements WarrantyAssigner {

    private final WarrantyStore repository;
    private final ProductService productService;
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and loads the warranties already
     * stored, resolving the sale and product of each one from the
     * identifiers kept by the repository.
     *
     * @param repository     repository used to persist warranty records
     * @param saleAssembler  assembler used to rebuild the stored sales
     * @param productService service used to resolve product references
     */
    public WarrantyService(WarrantyStore repository,
                           SaleAssembler saleAssembler,
                           ProductService productService) {
        this(repository, saleAssembler.loadSales(), productService);
    }

    /**
     * Creates the service from the stored sales already rebuilt.
     *
     * @param repository     repository used to persist warranty records
     * @param sales          stored sales, used to resolve the sale of each
     *                       warranty
     * @param productService service used to resolve product references
     */
    private WarrantyService(WarrantyStore repository, List<Sale> sales,
                            ProductService productService) {
        this.repository = repository;
        this.productService = productService;
        this.warranties = loadWarranties(sales);
    }

    /**
     * Loads the warranty records and resolves their product and sale
     * references, rebuilding the concrete warranty type indicated by the
     * discriminator. Records whose product or sale no longer exist are
     * skipped.
     *
     * @param sales stored sales, used to resolve the sale of each warranty
     * @return the warranties reconstructed from persistence
     */
    private List<Warranty> loadWarranties(List<Sale> sales) {

        List<Warranty> result = new ArrayList<>();

        for (WarrantyRecord record : repository.loadAll()) {

            Product product = productService.findById(record.productId());
            Sale sale = findSaleById(sales, record.saleId());

            if (product == null || sale == null) {
                continue;
            }

            WarrantyKind kind = findKind(record.type());

            if (kind != null) {
                result.add(kind.create(record.id(), product, sale, record.startDate()));
            }
        }

        return result;
    }

    /**
     * Finds the kind of warranty stored with the given code.
     *
     * @param code code written in the file ("BASIC", "EXTENDED", ...)
     * @return the kind, or {@code null} when the code is unknown, so that
     *         the record is skipped
     */
    private WarrantyKind findKind(String code) {
        for (WarrantyKind kind : WarrantyKind.values()) {
            if (kind.name().equals(code)) {
                return kind;
            }
        }
        return null;
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
    @Override
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
    @Override
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

            if (warranty.covers(productId, saleId)) {
                if (warranty.getKind().isPreferredCoverage()) {
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

        // At most one warranty of each kind is cancelled per returned unit.
        Map<WarrantyKind, Warranty> toCancel = new EnumMap<>(WarrantyKind.class);

        for (Warranty warranty : warranties) {

            if (warranty.covers(productId, saleId)) {
                toCancel.putIfAbsent(warranty.getKind(), warranty);
            }
        }

        double refundableCost = 0.0;

        for (Warranty warranty : toCancel.values()) {
            warranties.remove(warranty);
            refundableCost += warranty.getAdditionalCost();
        }

        if (!toCancel.isEmpty()) {
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

            if (warranty.belongsToSale(saleId)) {
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