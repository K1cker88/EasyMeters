package org.example.domain.business;

public class AccountHolderValidator {

    private static final int MIN_APT = 1, MAX_APT = 107;
    private static final int MIN_ACC = 1, MAX_ACC = 107;

    public void validate(String aptInput, String accInput) {
        int a = parse(aptInput, MIN_APT, MAX_APT, "номера квартиры");
        int c = parse(accInput, MIN_ACC, MAX_ACC, "номера лицевого счета");
    }

    private int parse(String in, int min, int max, String field) {
        try {
            int v = Integer.parseInt(in);
            if (v < min || v > max) {
                throw new IllegalArgumentException(
                        "Недопустимое значение " + field + ": " + v);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некорректный формат " + field + ": " + in);
        }
    }
}