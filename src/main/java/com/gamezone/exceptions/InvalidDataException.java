package com.gamezone.exceptions;

/**
 * Thrown when an input value does not meet the format, range or type
 * criteria, for example a negative price, a negative quantity or an empty
 * text where content is expected.
 */
public class InvalidDataException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "INVALID_DATA";

    /**
     * Creates the exception with a descriptive message in Spanish, for
     * example: "El campo 'precio' debe ser mayor que cero".
     *
     * @param fieldName   name of the invalid field, in Spanish
     * @param description description of the problem, in Spanish
     */
    public InvalidDataException(String fieldName, String description) {
        super("El campo '" + fieldName + "' " + description, ERROR_CODE);
    }
}