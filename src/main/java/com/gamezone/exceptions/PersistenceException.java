package com.gamezone.exceptions;

/**
 * Thrown when an error happens while reading or writing the data files of
 * the system, for example a corrupted file, a denied permission or a full
 * disk. It keeps the original exception as its cause so the technical trace
 * is not lost.
 */
public class PersistenceException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "PERSISTENCE_ERROR";

    /**
     * Creates the exception with a message and the original cause.
     *
     * @param message description of the error, including the file and the
     *                operation (reading or writing)
     * @param cause   original exception, usually an {@link java.io.IOException}
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, ERROR_CODE);
        initCause(cause);
    }
}