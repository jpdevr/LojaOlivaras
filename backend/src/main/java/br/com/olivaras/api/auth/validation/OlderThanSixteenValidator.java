package br.com.olivaras.api.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.ZoneId;

public class OlderThanSixteenValidator implements ConstraintValidator<OlderThanSixteen, LocalDate> {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) return true;
        LocalDate latestEligibleBirthDate = LocalDate.now(BUSINESS_ZONE).minusYears(16);
        return birthDate.isBefore(latestEligibleBirthDate);
    }
}
