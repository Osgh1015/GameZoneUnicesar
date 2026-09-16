package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Applies the business rules of the accessory module: registering the three
 * accessory types, listing and filtering the catalogue, answering
 * compatibility queries, and keeping accessory stock up to date.
 */
public class AccessoryService {

    private final AccessoryRepository repository;
    private final List<Accessory> accessories;

    /**
     * Creates the service and loads the accessories currently stored
     * through the given repository.
     *
     * @param repository repository used to persist and load accessories
     */
    public AccessoryService(AccessoryRepository repository) {
        this.repository = repository;
        this.accessories = repository.loadAll();
    }

    /**
     * Registers a new controller in the inventory and persists the change.
     *
     * @param controller controller to register
     */
    public void registerController(Controller controller) {
        accessories.add(controller);
        repository.saveAll(accessories);
    }

    /**
     * Registers a new cable in the inventory and persists the change.
     *
     * @param cable cable to register
     */
    public void registerCable(Cable cable) {
        accessories.add(cable);
        repository.saveAll(accessories);
    }

    /**
     * Registers a new memory unit in the inventory and persists the change.
     *
     * @param memory memory unit to register
     */
    public void registerMemory(Memory memory) {
        accessories.add(memory);
        repository.saveAll(accessories);
    }

    /**
     * Lists every accessory currently available in the inventory.
     *
     * @return an unmodifiable view of the accessories
     */
    public List<Accessory> listAllAccessories() {
        return Collections.unmodifiableList(accessories);
    }

    /**
     * Filters the accessories by their concrete type.
     *
     * @param type accessory type to filter by: CONTROLLER, CABLE or MEMORY
     * @return the accessories matching the requested type
     */
    public List<Accessory> listAccessoriesByType(String type) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (matchesType(accessory, type)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Finds every accessory declared compatible with a given console.
     *
     * @param consoleId id of the console to check compatibility against
     * @return the accessories compatible with that console
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (accessory.isCompatibleWith(consoleId)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Finds an accessory by its identifier.
     *
     * @param id identifier to search for
     * @return the matching accessory, or {@code null} if none is found
     */
    public Accessory findById(String id) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equals(id)) {
                return accessory;
            }
        }
        return null;
    }

    /**
     * Sets the stock of an accessory to a new value and persists the change.
     *
     * @param accessoryId identifier of the accessory
     * @param quantity    new quantity available in inventory
     * @throws NoSuchElementException if the accessory does not exist
     */
    public void updateStock(String accessoryId, int quantity) {
        Accessory accessory = findById(accessoryId);
        if (accessory == null) {
            throw new NoSuchElementException("Accessory not found: " + accessoryId);
        }
        accessory.setQuantity(quantity);
        repository.saveAll(accessories);
    }

    /**
     * Decreases the stock of an accessory after a sale is registered.
     *
     * @param accessoryId identifier of the accessory
     * @param amount      quantity to subtract from the inventory
     * @throws NoSuchElementException if the accessory does not exist
     * @throws IllegalStateException  if the available stock is insufficient
     */
    public void decreaseStock(String accessoryId, int amount) {
        Accessory accessory = findById(accessoryId);
        if (accessory == null) {
            throw new NoSuchElementException("Accessory not found: " + accessoryId);
        }
        if (accessory.getQuantity() < amount) {
            throw new IllegalStateException("Insufficient stock for accessory: " + accessoryId);
        }
        accessory.setQuantity(accessory.getQuantity() - amount);
        repository.saveAll(accessories);
    }

    /**
     * Checks whether an accessory matches the requested type name.
     *
     * @param accessory accessory to check
     * @param type      requested type name
     * @return {@code true} when the accessory is of the requested type
     */
    private boolean matchesType(Accessory accessory, String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase();
        return switch (normalized) {
            case "CONTROLLER" -> accessory instanceof Controller;
            case "CABLE" -> accessory instanceof Cable;
            case "MEMORY" -> accessory instanceof Memory;
            default -> false;
        };
    }
}
