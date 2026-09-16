package com.gamezone.model;

import java.util.List;

/**
 * Represents a memory card or storage unit sold by GameZone Unicesar.
 * Extends {@link Accessory} adding its storage capacity and memory type.
 */
public class Memory extends Accessory {

    private int capacityInGb;
    private String memoryType;

    /**
     * Creates a new memory unit.
     *
     * @param id                   unique identifier of the accessory
     * @param title                title of the memory unit
     * @param price                unit price
     * @param quantity             quantity available in inventory
     * @param compatibleConsoleIds ids of the consoles this memory works with
     * @param capacityInGb         storage capacity in gigabytes
     * @param memoryType           memory type (SD, microSD, internal card, etc.)
     */
    public Memory(String id, String title, double price, int quantity,
                  List<String> compatibleConsoleIds, int capacityInGb, String memoryType) {
        super(id, title, price, quantity, compatibleConsoleIds);
        this.capacityInGb = capacityInGb;
        this.memoryType = memoryType;
    }

    /**
     * @return the storage capacity in gigabytes
     */
    public int getCapacityInGb() {
        return capacityInGb;
    }

    /**
     * @param capacityInGb new storage capacity in gigabytes
     */
    public void setCapacityInGb(int capacityInGb) {
        this.capacityInGb = capacityInGb;
    }

    /**
     * @return the memory type
     */
    public String getMemoryType() {
        return memoryType;
    }

    /**
     * @param memoryType new memory type
     */
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * {@inheritDoc}
     * Adds the capacity and memory type to the shared accessory description.
     */
    @Override
    public String getDescription() {
        return String.format("Memoria: %s | Capacidad: %d GB | Tipo: %s | Precio: %.2f | Stock: %d | Compatible con: %s",
                getTitle(), capacityInGb, memoryType, getPrice(), getQuantity(), formatCompatibility());
    }
}
