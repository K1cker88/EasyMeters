package org.example.domain.model;

public class AccountHolder {
    private final int apartmentNumber;
    private final long accountNumber;
    private final long userId;

    public AccountHolder(int apartmentNumber, long accountNumber, long userId) {
        this.apartmentNumber = apartmentNumber;
        this.accountNumber = accountNumber;
        this.userId = userId;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public long getUserId() {
        return userId;
    }

}