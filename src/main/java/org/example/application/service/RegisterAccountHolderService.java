package org.example.application.service;

import org.example.domain.application.AccountHolderRepository;
import org.example.domain.application.RegisterAccountHolder;
import org.example.domain.business.AccountHolderValidator;
import org.example.domain.model.AccountHolder;
import org.springframework.stereotype.Service;

    @Service
    public class RegisterAccountHolderService implements RegisterAccountHolder {
        private final AccountHolderRepository repo;
        private final AccountHolderValidator validator = new AccountHolderValidator();
        public RegisterAccountHolderService(AccountHolderRepository repo) {
            this.repo = repo;
        }

        @Override
        public AccountHolder register(String aptInput, String accInput, long telegramUserId) {
            validator.validate(aptInput, accInput);
            int apt = Integer.parseInt(aptInput);
            int acc = Integer.parseInt(accInput);
            if (repo.existsByUserIdAndApartmentNumber(telegramUserId, apt)) {
                throw new IllegalStateException("Вы уже зарегистрированы.");
            }
            AccountHolder h = new AccountHolder(apt, acc, telegramUserId);
            repo.save(h);
            return h;
        }
    }