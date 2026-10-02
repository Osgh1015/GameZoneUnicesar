package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides the business operations related to warranties.
 *
 * It coordinates the creation, storage and consultation of warranty
 * objects while delegating file access to WarrantyRepository.
 *
 * Responsabilidad: Desarrollador 2 - Servicio de garantias.
 */
public class WarrantyService {

    private final WarrantyRepository repository;
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and loads the warranties already
     * stored in the repository.
     *
     * @param repository repository used to persist warranty data
     */
    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
        this.warranties = new ArrayList<>(repository.loadAll());
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
     * Finds the warranties associated with a specific product and sale.
     *
     * @param productId identifier of the product
     * @param saleId    identifier of the sale
     * @return list of warranties associated with the product and sale
     */
    public List<Warranty> findWarrantiesByProductAndSale(
            String productId,
            String saleId) {

        List<Warranty> result = new ArrayList<>();

        for (Warranty warranty : warranties) {

            if (warranty.getProduct().getId().equals(productId)
                    && warranty.getSale().getId().equals(saleId)) {

                result.add(warranty);
            }
        }

        return result;
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