package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Seller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
 * <p>
 * Any problem reading or writing a file is reported as a
 * {@link PersistenceException} that names the file and the operation.
 */
public class PersonRepository {

    private static final String DELIMITER = ";";

    private final String clientsFile;
    private final String vendorsFile;

    /**
     * Creates the repository that stores the people in
     * {@code data/clients.txt} and {@code data/vendors.txt}.
     *
     * @deprecated use {@link #PersonRepository(String, String)}; kept only
     *             until {@code Main} passes the file paths (V-05)
     */
    @Deprecated
    public PersonRepository() {
        this("data/clients.txt", "data/vendors.txt");
    }

    /**
     * Creates the repository that stores the people in the given files.
     *
     * @param clientsFile path of the clients file
     * @param vendorsFile path of the vendors file
     */
    public PersonRepository(String clientsFile, String vendorsFile) {
        this.clientsFile = clientsFile;
        this.vendorsFile = vendorsFile;
    }

    /**
     * Saves the given list of clients to the clients data file,
     * overwriting any previous content.
     *
     * @param clients the clients to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void saveClients(List<Client> clients) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(clientsFile))) {
            for (Client c : clients) {
                writer.println(c.getId() + DELIMITER + c.getName() + DELIMITER
                        + c.getPhone() + DELIMITER + c.getEmail());
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + clientsFile + " al guardar los clientes.", e);
        }
    }

    /**
     * Loads all clients previously saved in the clients data file.
     *
     * @return the list of clients found, or an empty list if the file
     *         does not exist yet
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     */
    public List<Client> loadClients() {
        List<Client> clients = new ArrayList<>();
        File file = new File(clientsFile);
        if (!file.exists()) {
            return clients;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                if (parts.length < 4) {
                    throw corruptedLine(clientsFile, lineNumber, "clientes");
                }
                clients.add(new Client(parts[0], parts[1], parts[2], parts[3]));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + clientsFile + " al cargar los clientes.", e);
        }
        return clients;
    }

    /**
     * Loads all vendors previously saved in the vendors data file.
     *
     * @return the list of vendors found, or an empty list if the file
     *         does not exist yet
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     */
    public List<Seller> loadVendors() {
        List<Seller> vendors = new ArrayList<>();
        File file = new File(vendorsFile);
        if (!file.exists()) {
            return vendors;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                if (parts.length < 5) {
                    throw corruptedLine(vendorsFile, lineNumber, "vendedores");
                }
                vendors.add(new Seller(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + vendorsFile + " al cargar los vendedores.", e);
        }
        return vendors;
    }

    /**
     * Builds the exception used when a line of a file does not have the
     * expected number of fields.
     *
     * @param fileName   file that contains the line
     * @param lineNumber number of the corrupted line
     * @param content    what the file stores, used in the message
     * @return the exception to throw
     */
    private PersistenceException corruptedLine(String fileName, int lineNumber, String content) {
        return new PersistenceException("El archivo " + fileName + " está dañado en la línea "
                + lineNumber + " (lectura de " + content + ").",
                new IOException("Número de campos incorrecto"));
    }
}
