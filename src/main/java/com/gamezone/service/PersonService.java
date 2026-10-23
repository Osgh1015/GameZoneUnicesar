package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.validation.PersonValidator;

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
 * <p>
 * The validations are delegated to {@link PersonValidator}, which throws
 * the custom exceptions of the system.
 */
public class PersonService {

    private final PersonRepository repository;
    private List<Client> clients;
    private List<Seller> vendors;

    /**
     * Creates the service with its own repository.
     *
     * @throws PersistenceException if a data file cannot be read
     * @deprecated use {@link #PersonService(PersonRepository)}; kept only
     *             until {@code Main} injects the repository (V-05)
     */
    @Deprecated
    public PersonService() {
        this(new PersonRepository());
    }

    /**
     * Creates the service, loading clients and vendors already stored by
     * the given repository.
     *
     * @param repository repository that stores clients and vendors
     * @throws PersistenceException if a data file cannot be read
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
     * @throws InvalidDataException  if a required field is empty or the email
     *                               does not have a valid format
     * @throws BusinessRuleException if a client or seller already uses the same id
     * @throws PersistenceException  if the clients file cannot be written
     */
    public void registerClient(Client client) {
        PersonValidator.validatePersonData(client.getId(), client.getName(),
                client.getEmail(), client.getPhone());
        PersonValidator.validateUniquePerson(clients, client.getId());
        PersonValidator.validateUniquePerson(vendors, client.getId());
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
    public List<Seller> listVendors() {
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
    public Seller findVendorById(String id) {
        for (Seller v : vendors) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public Seller findSellerById(String id) {
        for (Seller v : vendors) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public Seller[] listSellers() {
        return vendors.toArray(new Seller[0]);
    }
}