package com.gamezone.exceptions;

/**
 * Root of the hierarchy of custom exceptions of GameZone Unicesar.
 * <p>
 * Every error of the system is represented by one of its subclasses, so the
 * user interface can catch each category of error and show a clear message.
 * It extends {@link RuntimeException} (unchecked), so the methods of the
 * services and repositories do not need to declare it with {@code throws}.
 * The class is abstract because an error without a category has no meaning
 * in the system: only its specialized subclasses are thrown.
 */
public abstract class GameZoneException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    /**
     * Creates an exception with a message and no error code.
     *
     * @param message description of the error, in Spanish
     */
    public GameZoneException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * Creates an exception with a message and an error code.
     *
     * @param message   description of the error, in Spanish
     * @param errorCode short code that identifies the category of the error
     */
    public GameZoneException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the code that identifies the category of the error.
     *
     * @return the error code, or {@code null} if none was assigned
     */
    public String getErrorCode() {
        return errorCode;
    }
}