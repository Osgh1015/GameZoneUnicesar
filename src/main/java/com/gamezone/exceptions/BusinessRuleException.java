package com.gamezone.exceptions;

/**
 * Thrown when an operation violates a business rule of the store, for
 * example insufficient stock, a duplicated client or a sale without
 * products.
 */
public class BusinessRuleException extends GameZoneException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "BUSINESS_RULE";

    /**
     * Creates the exception with the description of the violated rule.
     *
     * @param message description of the violated rule, in Spanish
     */
    public BusinessRuleException(String message) {
        super(message, ERROR_CODE);
    }
}