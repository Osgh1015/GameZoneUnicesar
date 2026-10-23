package com.gamezone.model;

import java.util.List;

/**
 * Represents a game controller sold by GameZone Unicesar.
 * Extends {@link Accessory} adding the connection type that distinguishes
 * a wireless controller from a wired one.
 */
public class Controller extends Accessory {

    private String connectionType;

    /**
     * Creates a new controller.
     *
     * @param id                   unique identifier of the accessory
     * @param title                title of the controller
     * @param price                unit price
     * @param quantity             quantity available in inventory
     * @param compatibleConsoleIds ids of the consoles this controller works with
     * @param connectionType       connection type (wireless or wired)
     */
    public Controller(String id, String title, double price, int quantity,
                      List<String> compatibleConsoleIds, String connectionType) {
        super(id, title, price, quantity, compatibleConsoleIds);
        this.connectionType = connectionType;
    }

    /**
     * @return the connection type of the controller
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * @param connectionType new connection type
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * {@inheritDoc}
     * Adds the connection type to the shared accessory description.
     */
    @Override
    public String getDescription() {
        return String.format("Control: %s | Conexión: %s | Precio: %.2f | Stock: %d | Compatible con: %s",
                getTitle(), connectionType, getPrice(), getQuantity(), formatCompatibility());
    }
}
