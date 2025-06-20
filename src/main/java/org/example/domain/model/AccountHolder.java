package org.example.domain.model;

import java.util.Objects;

public class AccountHolder {
    private final int apartmentNumber;
    private final int accountNumber;
    private final long userId;

    public AccountHolder(int apartmentNumber, int accountNumber, long userId) {
        this.apartmentNumber = apartmentNumber;
        this.accountNumber = accountNumber;
        this.userId = userId;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public long getUserId() {
        return userId;
    }

}