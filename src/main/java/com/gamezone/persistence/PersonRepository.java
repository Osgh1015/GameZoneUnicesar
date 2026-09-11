package com.gamezone.persistence;

import com.gamezone.model.Client;
import com.gamezone.model.Vendor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing Client and Vendor data to plain text
 * (CSV-like) files, so information survives between executions.
 * <p>
 * This class belongs to the persistence layer: it is the only place in
 * the person module that knows about files and file formats. The model
 * classes (Person, Client, Vendor) know nothing about how or where they
 * are stored, and the service layer only talks to this repository
 * through simple method calls (save/load), never touching files
 * directly.
 */
public class PersonRepository {

    private static final String CLIENTS_FILE = "data/clients.txt";
    private static final String VENDORS_FILE = "data/vendors.txt";

    /**
     * Saves the given list of clients to the clients data file,
     * overwriting any previous content.
     *
     * @param clients the clients to persist
     */
    public void saveClients(List<Client> clients) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CLIENTS_FILE))) {
            for (Client c : clients) {
                writer.println(c.getId() + ";" + c.getName() + ";" + c.getPhone() + ";" + c.getEmail());
            }
        } catch (IOException e) {
            System.out.println("Error saving clients: " + e.getMessage());
        }
    }

    /**
     * Loads all clients previously saved in the clients data file.
     *
     * @return the list of clients found, or an empty list if the file
     *         does not exist yet
     */
    public List<Client> loadClients() {
        List<Client> clients = new ArrayList<>();
        File file = new File(CLIENTS_FILE);
        if (!file.exists()) {
            return clients;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                clients.add(new Client(parts[0], parts[1], parts[2], parts[3]));
            }
        } catch (IOException e) {
            System.out.println("Error loading clients: " + e.getMessage());
        }
        return clients;
    }

    /**
     * Saves the given list of vendors to the vendors data file,
     * overwriting any previous content.
     *
     * @param vendors the vendors to persist
     */
    public void saveVendors(List<Vendor> vendors) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(VENDORS_FILE))) {
            for (Vendor v : vendors) {
                writer.println(v.getId() + ";" + v.getName() + ";" + v.getPhone() + ";"
                        + v.getEmployeeCode() + ";" + v.getWorkShift());
            }
        } catch (IOException e) {
            System.out.println("Error saving vendors: " + e.getMessage());
        }
    }

    /**
     * Loads all vendors previously saved in the vendors data file.
     *
     * @return the list of vendors found, or an empty list if the file
     *         does not exist yet
     */
    public List<Vendor> loadVendors() {
        List<Vendor> vendors = new ArrayList<>();
        File file = new File(VENDORS_FILE);
        if (!file.exists()) {
            return vendors;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                vendors.add(new Vendor(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (IOException e) {
            System.out.println("Error loading vendors: " + e.getMessage());
        }
        return vendors;
    }
}