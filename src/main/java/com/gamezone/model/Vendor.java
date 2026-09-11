package com.gamezone.model;


public class Vendor extends Person {

    private String employeeCode;
    private String workShift;


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


    @Override
    public String describe() {
        return "Vendor: " + getName() + " (Employee code: " + employeeCode
                + ", Shift: " + workShift + ")";
    }
}
