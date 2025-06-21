package org.example.domain.application;

import org.example.domain.model.AccountHolder;

public interface AccountHolderRepositoryPort {
    void save(AccountHolder holder);
    boolean existsByUserIdAndApartmentNumber(long userId, int apartmentNumber);
}