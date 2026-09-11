package com.gamezone.model;

/**
 * Represents a vendor (employee) of the store, in charge of attending
 * clients and registering sales.
 * <p>
 * In addition to the attributes shared with every person, a Vendor has
 * an employee code and an assigned work shift. Vendors are preloaded by
 * the team (they are already hired staff) instead of being registered
 * through the user interface.
 */
public class Vendor extends Person {

    private String employeeCode;
    private String workShift;

    /**
     * Creates a new vendor.
     *
     * @param id           unique identification of the vendor
     * @param name         full name of the vendor
     * @param phone        contact phone number
     * @param employeeCode internal employee code
     * @param workShift    assigned work shift (e.g. "Morning", "Afternoon")
     */
    public Vendor(String id, String name, String phone, String employeeCode, String workShift) {
        super(id, name, phone);
        this.employeeCode = employeeCode;
        this.workShift = workShift;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return "Vendor: " + getName() + " (Employee code: " + employeeCode
                + ", Shift: " + workShift + ")";
    }
}