package com.gamezone.exceptions;

/**
 * Thrown when a resource (product, client, seller, sale...) is searched by
 * its identifier and it does not exist in the system.
 */
public class ResourceNotFoundException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Creates the exception with a descriptive message in Spanish, for
     * example: "El producto con identificador P009 no existe en el sistema".
     *
     * @param resourceType type of resource with its article, in Spanish
     *                     (for example "el producto" or "la venta")
     * @param id           identifier that was searched
     */
    public ResourceNotFoundException(String resourceType, String id) {
        super(capitalize(resourceType) + " con identificador " + id + " no existe en el sistema.",
                ERROR_CODE);
    }

    /**
     * Converts the first letter of a text to upper case.
     *
     * @param text text to convert
     * @return the text with its first letter in upper case
     */
    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "El recurso";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}