package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for every accessory sold by GameZone Unicesar.
 * Extends {@link Product} so an accessory can be sold in the same
 * transaction as a video game or a console, and adds the list of consoles
 * the accessory is compatible with.
 */
public abstract class Accessory extends Product {

    private List<String> compatibleConsoleIds;

    /**
     * Creates a new accessory.
     *
     * @param id                   unique identifier of the accessory
     * @param title                title of the accessory
     * @param price                unit price
     * @param quantity             quantity available in inventory
     * @param compatibleConsoleIds ids of the consoles this accessory works with
     */
    public Accessory(String id, String title, double price, int quantity,
                     List<String> compatibleConsoleIds) {
        super(id, title, price, quantity);
        this.compatibleConsoleIds = compatibleConsoleIds != null
                ? compatibleConsoleIds
                : new ArrayList<>();
    }

    /**
     * @return the ids of the consoles this accessory is compatible with
     */
    public List<String> getCompatibleConsoleIds() {
        return compatibleConsoleIds;
    }

    /**
     * Replaces the full list of compatible consoles.
     *
     * @param compatibleConsoleIds new list of compatible console ids
     */
    public void setCompatibleConsoleIds(List<String> compatibleConsoleIds) {
        this.compatibleConsoleIds = compatibleConsoleIds;
    }

    /**
     * Registers one more console as compatible with this accessory.
     * Duplicated ids are ignored.
     *
     * @param consoleId id of the console to add
     */
    public void addCompatibleConsole(String consoleId) {
        if (!compatibleConsoleIds.contains(consoleId)) {
            compatibleConsoleIds.add(consoleId);
        }
    }

    /**
     * Removes a console from the compatibility list of this accessory.
     *
     * @param consoleId id of the console to remove
     */
    public void removeCompatibleConsole(String consoleId) {
        compatibleConsoleIds.remove(consoleId);
    }

    /**
     * Checks whether this accessory is compatible with a given console.
     *
     * @param consoleId id of the console to check
     * @return {@code true} if the console is in the compatibility list
     */
    public boolean isCompatibleWith(String consoleId) {
        return compatibleConsoleIds.contains(consoleId);
    }

    /**
     * Builds the part of the description that every accessory shares,
     * including the list of compatible consoles. Concrete subclasses call
     * this and append their own specific attributes.
     *
     * @return the common accessory description
     */
    @Override
    public String getDescription() {
        return String.format("%s | Precio: %.2f | Stock: %d | Compatible con: %s",
                getTitle(), getPrice(), getQuantity(), formatCompatibility());
    }

    /**
     * Formats the compatibility list for display.
     *
     * @return the compatible console ids joined by commas, or a placeholder
     */
    protected String formatCompatibility() {
        if (compatibleConsoleIds.isEmpty()) {
            return "ninguna consola registrada";
        }
        return String.join(", ", compatibleConsoleIds);
    }
}
