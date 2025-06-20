package org.example.domain.application;

import org.example.domain.model.AccountHolder;

public interface RegisterAccountHolder {
    AccountHolder register(String apartmentInput, String accountInput, long telegramUserId);
}