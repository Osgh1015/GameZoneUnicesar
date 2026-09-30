package com.gamezone.service;

import com.gamezone.model.*;
import com.gamezone.persistence.ReturnRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Contains the business rules for registering and consulting returns:
 * a return must reference an existing sale within its 30-day window,
 * the returned products must belong to that sale, and stock must be
 * restored automatically when a return is confirmed.
 */
public class ReturnService {

    private final ReturnRepository repository;
    private final SaleService saleService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final List<Return> returns;

    public ReturnService(ReturnRepository repository, SaleService saleService,
                         ProductService productService, AccessoryService accessoryService) {
        this.repository = repository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.returns = repository.loadAll();
    }

    /**
     * Registers a new return after validating the 30-day deadline and
     * that every requested product actually belongs to the original sale.
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
     * Calculates the net balance for the given month and year:
     * total sales minus total returns in that period.
     * @param month the month (1-12)
     * @param year  the year
     * @return the net balance (sales total - returns total)
     */
    public double generateMonthlyBalance(int month, int year) {
        double totalSales = 0.0;
        for (Sale sale : saleService.getSalesHistory()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.calculateTotal();
            }
        }

        double totalReturns = 0.0;
        for (Return r : returns) {
            if (r.getDate().getMonthValue() == month && r.getDate().getYear() == year) {
                totalReturns += r.getRefundAmount();
            }
        }

        return totalSales - totalReturns;
    }
}