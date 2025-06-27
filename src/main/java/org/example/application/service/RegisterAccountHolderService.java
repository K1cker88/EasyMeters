package org.example.application.service;

import org.example.domain.application.AccountHolderRepositoryPort;
import org.example.domain.application.RegisterAccountHolderUseCase;
import org.example.domain.business.AccountHolderValidator;
import org.example.domain.model.AccountHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccountHolderService
        implements RegisterAccountHolderUseCase {

    private final AccountHolderRepositoryPort repo;
    private final AccountHolderValidator validator = new AccountHolderValidator();

    public RegisterAccountHolderService(AccountHolderRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public AccountHolder register(String aptInput, String accInput, long telegramUserId) {
        validator.validate(aptInput, accInput);
        int apt = Integer.parseInt(aptInput);
        int acc = Integer.parseInt(accInput);
        if (repo.existsByUserIdAndApartmentNumber(telegramUserId, apt)) {
            throw new IllegalStateException("Вы уже зарегистрированы для этой квартиры.");
        }
        AccountHolder h = new AccountHolder(apt, acc, telegramUserId);
        try {
            repo.save(h);
        } catch (IllegalStateException ex) {
            // тут мы перехватываем дубликат из репозитория
            throw new IllegalStateException(ex.getMessage());
        }
        return h;
    }
}