package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Vendor;
import com.gamezone.persistence.PersonRepository;

import java.util.List;


public class PersonService {

    private final PersonRepository repository;
    private List<Client> clients;
    private List<Vendor> vendors;


    public PersonService(PersonRepository repository) {
        this.repository = repository;
        this.clients = repository.loadClients();
        this.vendors = repository.loadVendors();
    }


    public void registerClient(Client client) {
        clients.add(client);
        repository.saveClients(clients);
    }


    public List<Client> listClients() {
        return clients;
    }


    public List<Vendor> listVendors() {
        return vendors;
    }


    public Client findClientById(String id) {
        for (Client c : clients) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }


    public Vendor findVendorById(String id) {
        for (Vendor v : vendors) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }
}
