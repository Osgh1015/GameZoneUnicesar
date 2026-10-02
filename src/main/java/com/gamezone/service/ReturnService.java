package com.gamezone.service;

import com.gamezone.model.*;
import com.gamezone.persistence.ReturnRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Contains the business rules for registering and consulting returns:
 * a return must reference an existing sale within its 30-day window,
 * the returned products must belong to that sale, stock must be
 * restored automatically when a return is confirmed, and every returned
 * console loses its warranties (the extended warranty cost is refunded).
 */
public class ReturnService {

    private final ReturnRepository repository;
    private final SaleService saleService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final WarrantyService warrantyService;
    private final List<Return> returns;

    /**
     * Creates the return service with its collaborators, injected by
     * constructor from {@code Main}.
     *
     * @param repository       repository used to persist returns
     * @param saleService      service used to find the original sales
     * @param productService   service used to restore the stock of products
     * @param accessoryService service used to restore the stock of accessories
     * @param warrantyService  service used to cancel the warranties of
     *                         returned consoles
     */
    public ReturnService(ReturnRepository repository, SaleService saleService,
                         ProductService productService, AccessoryService accessoryService,
                         WarrantyService warrantyService) {
        this.repository = repository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
        this.returns = repository.loadAll();
    }

    /**
     * Registers a new return after validating the 30-day deadline and
     * that every requested product actually belongs to the original sale.
     * The warranties of every returned console are cancelled through
     * {@link WarrantyService#cancelWarranties(String, String)} and the
     * refundable cost is included in the refund amount.
     *
     * @param saleId     id of the original sale
     * @param productIds ids of the products being returned
     * @param reason     reason for the return
     * @return the registered Return
     * @throws IllegalArgumentException if any validation fails
     */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("La venta no existe.");
        }

        if (!sale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "La devolución excede los 30 días permitidos desde la fecha de venta.");
        }

        List<Product> productsToReturn = new ArrayList<>();
        for (String productId : productIds) {
            Product match = null;
            for (Product p : sale.getProducts()) {
                if (p.getId().equals(productId)) {
                    match = p;
                    break;
                }
            }
            if (match == null) {
                throw new IllegalArgumentException(
                        "El producto " + productId + " no pertenece a la venta indicada.");
            }
            productsToReturn.add(match);
        }

        String newId = UUID.randomUUID().toString();
        Return newReturn = new Return(newId, LocalDate.now(), sale, productsToReturn, reason);

        // A returned console cannot keep a valid warranty: cancel its
        // warranties and refund the cost of the extended one (if any).
        double warrantyRefund = 0.0;
        for (Product p : productsToReturn) {
            if (p instanceof Console) {
                warrantyRefund += warrantyService.cancelWarranties(p.getId(), sale.getId());
            }
        }
        newReturn.setWarrantyRefund(Math.round(warrantyRefund * 100.0) / 100.0);
        newReturn.calculateRefundAmount();

        for (Product p : productsToReturn) {
            if (p instanceof Accessory) {
                accessoryService.restoreStock(p.getId(), 1);
            } else {
                productService.restoreStock(p.getId(), 1);
            }
        }

        returns.add(newReturn);
        repository.saveAll(returns);

        return newReturn;
    }

    /** @return every return registered in the system */
    public List<Return> viewAllReturns() {
        return new ArrayList<>(returns);
    }

    /**
     * @param customerId id of the client
     * @return returns whose original sale belongs to the given client
     */
    public List<Return> viewReturnsByCustomer(String customerId) {
        List<Return> result = new ArrayList<>();
        for (Return r : returns) {
            if (r.getOriginalSale().getClient().getId().equals(customerId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * @param saleId id of the sale
     * @return returns associated with the given sale
     */
    public List<Return> viewReturnsBySale(String saleId) {
        List<Return> result = new ArrayList<>();
        for (Return r : returns) {
            if (r.getOriginalSale().getId().equals(saleId)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Calculates the total sold in the given month and year. Each sale
     * contributes its final total (subtotal - discount + extended warranty
     * cost), which is the amount the client actually paid.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the final totals of the sales of that period
     */
    public double calculateMonthlySales(int month, int year) {
        double totalSales = 0.0;
        for (Sale sale : saleService.getSalesHistory()) {
            if (isInPeriod(sale.getDate(), month, year)) {
                totalSales += sale.calculateFinalTotal();
            }
        }
        return round(totalSales);
    }

    /**
     * Calculates the total refunded in the given month and year, adding
     * the refund amount of every return registered in that period.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the refunds of the returns of that period
     */
    public double calculateMonthlyReturns(int month, int year) {
        double totalReturns = 0.0;
        for (Return r : returns) {
            if (isInPeriod(r.getDate(), month, year)) {
                totalReturns += r.getRefundAmount();
            }
        }
        return round(totalReturns);
    }

    /**
     * Calculates the net balance for the given month and year: total
     * sales minus total returns in that period.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the net balance (monthly sales - monthly returns)
     */
    public double generateMonthlyBalance(int month, int year) {
        return round(calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year));
    }

    /**
     * Checks whether a date belongs to the given month and year.
     *
     * @param date  date to check
     * @param month the month (1-12)
     * @param year  the year
     * @return true if the date is in that month of that year
     */
    private boolean isInPeriod(LocalDate date, int month, int year) {
        return date.getMonthValue() == month && date.getYear() == year;
    }

    /**
     * Rounds a monetary value to two decimals.
     *
     * @param value value to round
     * @return the rounded value
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}