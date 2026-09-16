package com.gamezone.model;

import java.util.List;

/**
 * Represents a cable sold by GameZone Unicesar.
 * Extends {@link Accessory} adding its physical length and the type of
 * connector it uses.
 */
public class Cable extends Accessory {

    private double lengthInMeters;
    private String connectorType;

    /**
     * Creates a new cable.
     *
     * @param id                   unique identifier of the accessory
     * @param title                title of the cable
     * @param price                unit price
     * @param quantity             quantity available in inventory
     * @param compatibleConsoleIds ids of the consoles this cable works with
     * @param lengthInMeters       length of the cable in meters
     * @param connectorType        connector type (HDMI, USB, optical, etc.)
     */
    public Cable(String id, String title, double price, int quantity,
                 List<String> compatibleConsoleIds, double lengthInMeters, String connectorType) {
        super(id, title, price, quantity, compatibleConsoleIds);
        this.lengthInMeters = lengthInMeters;
        this.connectorType = connectorType;
    }

    /**
     * @return the length of the cable in meters
     */
    public double getLengthInMeters() {
        return lengthInMeters;
    }

    /**
     * @param lengthInMeters new length in meters
     */
    public void setLengthInMeters(double lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    /**
     * @return the connector type of the cable
     */
    public String getConnectorType() {
        return connectorType;
    }

    /**
     * @param connectorType new connector type
     */
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    /**
     * {@inheritDoc}
     * Adds the length and connector type to the shared accessory description.
     */
    @Override
    public String getDescription() {
        return String.format("Cable: %s | Longitud: %.2f m | Conector: %s | Precio: %.2f | Stock: %d | Compatible con: %s",
                getTitle(), lengthInMeters, connectorType, getPrice(), getQuantity(), formatCompatibility());
    }
}
