package org.example.domain.business;

public class AccountHolderValidator {

    private static final int MIN_APT = 1, MAX_APT = 107;
    private static final long  MIN_ACC = 43010010001L, MAX_ACC = 43010010107L;

    public void validate(String aptInput, String accInput) {
       long apt = parse(aptInput, MIN_APT, MAX_APT, "номера квартиры");
       long acc = parse(accInput, MIN_ACC, MAX_ACC, "номера лицевого счета");

        String aptStr = String.valueOf(apt);
        if (!accInput.endsWith(aptStr)) {
            throw new IllegalArgumentException(
                    "Указанный лицевой счет не соотвествует введенному номеру квартиры");
        }
    }
    private long parse(String in, long min, long max, String field) {
        try {
            long v = Long.parseLong(in);
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