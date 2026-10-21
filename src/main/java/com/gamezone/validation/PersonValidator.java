package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Person;

import java.util.List;

/**
 * Validations of the people module (clients and sellers). It is called by
 * the services before running any business operation on people.
 * <p>
 * It only contains static methods, uses only model classes and exceptions,
 * and never calls services or repositories: when it needs the existing
 * people, the service passes them as a parameter.
 */
public final class PersonValidator {

    /**
     * Prevents creating instances: this class only has static methods.
     */
    private PersonValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates the data of a person before registering it.
     *
     * @param id    identifier of the person
     * @param name  full name of the person
     * @param email email of the person
     * @param phone phone number of the person
     * @throws InvalidDataException if a required field is empty or the email
     *                              does not have a reasonable format
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "no puede estar vacío.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("nombre", "no puede estar vacío.");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("teléfono", "no puede estar vacío.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("correo electrónico", "no puede estar vacío.");
        }
        if (!hasReasonableEmailFormat(email.trim())) {
            throw new InvalidDataException("correo electrónico",
                    "debe tener un formato válido, por ejemplo nombre@correo.com.");
        }
    }

    /**
     * Validates that a person searched by its identifier exists.
     *
     * @param person   result of the search, {@code null} if it was not found
     * @param personId identifier that was searched
     * @throws ResourceNotFoundException if the person is null
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("la persona", personId);
        }
    }

    /**
     * Validates that no other person already uses the given identifier.
     *
     * @param existing people already registered (clients or sellers)
     * @param newId    identifier of the person to register
     * @throws BusinessRuleException if another person has the same identifier
     */
    public static void validateUniquePerson(List<? extends Person> existing, String newId) {
        if (existing == null) {
            return;
        }
        for (Person person : existing) {
            if (person.getId().equals(newId)) {
                throw new BusinessRuleException(
                        "Ya existe una persona registrada con el identificador " + newId + ".");
            }
        }
    }

    /**
     * Checks that an email contains an at sign with text before it, and a
     * dot after the at sign that is not the last character.
     *
     * @param email email to check
     * @return {@code true} if the email has a reasonable format
     */
    private static boolean hasReasonableEmailFormat(String email) {
        int atPosition = email.indexOf('@');
        int lastDotPosition = email.lastIndexOf('.');
        return atPosition > 0
                && lastDotPosition > atPosition + 1
                && lastDotPosition < email.length() - 1;
    }
}