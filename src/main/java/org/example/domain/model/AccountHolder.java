package org.example.domain.model;

import java.util.Objects;

public class AccountHolder {
    private final int apartmentNumber;
    private final int accountNumber;
    private final long userId;

    public AccountHolder(int apartmentNumber, int accountNumber, long userId) {
        this.apartmentNumber = apartmentNumber;
        this.accountNumber   = accountNumber;
        this.userId          = userId;
    }

    public int getApartmentNumber() { return apartmentNumber; }
    public int getAccountNumber()   { return accountNumber; }
    public long getUserId()         { return userId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountHolder)) return false;
        AccountHolder that = (AccountHolder) o;
        return apartmentNumber == that.apartmentNumber &&
                accountNumber   == that.accountNumber &&
                userId          == that.userId;
    }
    @Override public int hashCode() {
        return Objects.hash(apartmentNumber, accountNumber, userId);
    }
}