package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Vendor;
import com.gamezone.persistence.PersonRepository;

import java.util.List;

/**
 * Contains the business rules related to clients and vendors, and is the
 * only class in this module authorized to call the persistence layer.
 * <p>
 * The user interface never talks to {@link PersonRepository} directly:
 * every operation on people (registering a client, listing clients or
 * vendors) goes through this service, which loads data on startup and
 * saves it after every change, keeping the in-memory lists and the
 * files in sync.
 */
public class PersonService {

    private final PersonRepository repository;
    private List<Client> clients;
    private List<Vendor> vendors;

    /**
     * Creates the service, loading clients and vendors already stored
     * on disk.
     *
     * @param repository the repository used to read/write person data
     */
    public PersonService(PersonRepository repository) {
        this.repository = repository;
        this.clients = repository.loadClients();
        this.vendors = repository.loadVendors();
    }

    /**
     * Registers a new client and immediately persists the updated list.
     *
     * @param client the client to register
     */
    public void registerClient(Client client) {
        clients.add(client);
        repository.saveClients(clients);
    }

    /**
     * Returns all clients currently registered in the store.
     *
     * @return the list of clients
     */
    public List<Client> listClients() {
        return clients;
    }

    /**
     * Returns all vendors currently working in the store. Vendors are
     * not registered through this service: they are preloaded data, so
     * this method only exposes them for consultation.
     *
     * @return the list of vendors
     */
    public List<Vendor> listVendors() {
        return vendors;
    }

    /**
     * Looks up a client by its identification.
     *
     * @param id the client id to search for
     * @return the matching client, or {@code null} if not found
     */
    public Client findClientById(String id) {
        for (Client c : clients) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Looks up a vendor by its identification.
     *
     * @param id the vendor id to search for
     * @return the matching vendor, or {@code null} if not found
     */
    public Vendor findVendorById(String id) {
        for (Vendor v : vendors) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }
}