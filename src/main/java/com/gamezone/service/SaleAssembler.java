package com.gamezone.service;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SaleStore;
import com.gamezone.persistence.SalePersistence.SaleRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Rebuilds the stored sales from the references kept by
 * {@link SaleStore}: it resolves the client, the seller and every
 * item of each record through the services that own them.
 * <p>
 * Both the sales module and the warranty module need the stored sales when
 * they start. Keeping the resolution here (Pure Fabrication) lets the
 * persistence layer depend on no service, and lets the warranty module
 * rebuild the sales without depending on {@link SaleService}, which would
 * create a cycle.
 */
public class SaleAssembler {

    private final SaleStore salePersistence;
    private final PersonService personService;
    private final ItemCatalog itemCatalog;

    /**
     * Creates the assembler.
     *
     * @param salePersistence persistence that reads the sale records
     * @param personService   service used to resolve clients and sellers
     * @param itemCatalog     intermediary used to resolve the items sold
     */
    public SaleAssembler(SaleStore salePersistence, PersonService personService,
                         ItemCatalog itemCatalog) {
        this.salePersistence = salePersistence;
        this.personService = personService;
        this.itemCatalog = itemCatalog;
    }

    /**
     * Reads every stored sale and rebuilds it. Records whose client or seller
     * no longer exist are skipped, and so are items that no catalog has any
     * more.
     *
     * @return the sales rebuilt from persistence
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     */
    public List<Sale> loadSales() {
        List<Sale> sales = new ArrayList<>();
        for (SaleRecord record : salePersistence.loadAll()) {
            Sale sale = assemble(record);
            if (sale != null) {
                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Rebuilds one sale, reporting a corrupted amount as a damaged line of
     * the sales file.
     *
     * @param record stored references of the sale
     * @return the sale, or {@code null} if its client or seller no longer exist
     * @throws PersistenceException if an amount of the record is not a number
     */
    private Sale assemble(SaleRecord record) {
        try {
            Client client = personService.findClientById(record.clientId());
            Seller seller = personService.findSellerById(record.sellerId());
            if (client == null || seller == null) {
                return null;
            }
            Sale sale = new Sale(record.id(), record.date(), client, seller);
            for (String itemId : record.itemIds()) {
                Product item = itemCatalog.findItem(itemId);
                if (item != null) {
                    sale.addProduct(item);
                }
            }
            if (record.appliedPromotionName() != null) {
                sale.setAppliedPromotionName(record.appliedPromotionName());
            }
            if (record.discountAmount() != null) {
                sale.setDiscountAmount(Double.parseDouble(record.discountAmount()));
            }
            if (record.warrantyCost() != null) {
                sale.addWarrantyCost(Double.parseDouble(record.warrantyCost()));
            }
            return sale;
        } catch (RuntimeException e) {
            throw salePersistence.damagedLine(record.lineNumber(), e);
        }
    }
}
