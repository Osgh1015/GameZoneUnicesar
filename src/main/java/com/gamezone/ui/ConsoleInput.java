package com.gamezone.ui;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Reads what the user types in the console. Every menu of the application
 * receives the same instance, so all of them read from one scanner over
 * {@code System.in}, and the numeric readers are written in one place.
 */
public class ConsoleInput {

    private final Scanner scanner;

    /**
     * Creates the reader over the given scanner.
     *
     * @param scanner scanner over the console input
     */
    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads the next line typed by the user.
     *
     * @return the line, without the line terminator
     */
    public String nextLine() {
        return scanner.nextLine();
    }

    /**
     * Shows a prompt and reads a date in ISO format (AAAA-MM-DD).
     *
     * @param prompt text shown before reading
     * @return the date typed by the user
     * @throws java.time.format.DateTimeParseException if the text is not a valid date
     */
    public LocalDate readDate(String prompt) {
        System.out.print(prompt);
        return LocalDate.parse(scanner.nextLine().trim());
    }

    /**
     * Reads a decimal number that is zero or positive. If the user types
     * something invalid, the question is repeated instead of closing the
     * application.
     *
     * @return the number typed by the user
     */
    public double readDouble() {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= 0) {
                    return value;
                }
                System.out.print("El valor no puede ser negativo. Intente de nuevo: ");
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, ingrese un número: ");
            }
        }
    }

    /**
     * Reads an integer that is zero or positive. If the user types
     * something invalid, the question is repeated instead of closing the
     * application.
     *
     * @return the number typed by the user
     */
    public int readInt() {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= 0) {
                    return value;
                }
                System.out.print("El valor no puede ser negativo. Intente de nuevo: ");
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, ingrese un número entero: ");
            }
        }
    }
}
