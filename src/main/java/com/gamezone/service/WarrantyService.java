package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
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

    private final WarrantyRepository warrantyRepository;
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and loads the warranties already
     * stored in the repository.
     *
     * @param warrantyRepository repository used to persist warranty data
     */
    public WarrantyService(
            WarrantyRepository warrantyRepository) {

        this.warrantyRepository =
                warrantyRepository;

        this.warranties =
                new ArrayList<>(
                        warrantyRepository.loadAll()
                );
    }

    /**
     * Registers the basic warranty associated with a product in a sale.
     *
     * @param product   product covered by the warranty
     * @param sale      sale associated with the warranty
     * @param startDate date when the warranty starts
     * @return the basic warranty created
     */
    public BasicWarranty registerBasicWarranty(
            Product product,
            Sale sale,
            LocalDate startDate) {

        BasicWarranty warranty =
                new BasicWarranty(
                        generateWarrantyId(),
                        product,
                        sale,
                        startDate
                );

        warranties.add(warranty);

        warrantyRepository.saveAll(
                warranties
        );

        return warranty;
    }

    /**
     * Generates a unique identifier for a warranty.
     *
     * @return generated warranty identifier
     */
    private String generateWarrantyId() {

        return "W-" + UUID.randomUUID();
    }
}