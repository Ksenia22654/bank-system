package com.bank.model;

import java.sql.Date;

/** Клиент банка — физическое лицо. */
public class Client extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDate;
    private String passportNo;
    private String phone;
    private String email;
    private String address;
    private Date registeredOn;

    public String getFullName() {
        StringBuilder name = new StringBuilder(lastName).append(' ').append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            name.append(' ').append(middleName);
        }
        return name.toString();
    }

    @Override
    public String getTitle() {
        return getFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public void setPassportNo(String passportNo) {
        this.passportNo = passportNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(Date registeredOn) {
        this.registeredOn = registeredOn;
    }
}
