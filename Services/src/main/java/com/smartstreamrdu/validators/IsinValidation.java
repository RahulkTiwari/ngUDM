package com.smartstreamrdu.validators;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.validator.routines.CodeValidator;
import org.apache.commons.validator.routines.checkdigit.ISINCheckDigit;

public class IsinValidation implements Serializable {

    private static final long serialVersionUID = -5964391439144260936L;

    private static final String ISIN_REGEX = "([A-Z]{2}[A-Z0-9]{9}[0-9])";

    private static final CodeValidator VALIDATOR = new CodeValidator(ISIN_REGEX, 12, ISINCheckDigit.ISIN_CHECK_DIGIT);

    private static final IsinValidation ISIN_VALIDATOR_FALSE = new IsinValidation(false);

    private static final IsinValidation ISIN_VALIDATOR_TRUE = new IsinValidation(true);

    private static final String [] CCODES = Locale.getISOCountries();

 
    static {
        Arrays.sort(CCODES); 
    }

    private final boolean checkCountryCode;

    public static  IsinValidation getInstance(boolean checkCountryCode) {
        return checkCountryCode ? ISIN_VALIDATOR_TRUE : ISIN_VALIDATOR_FALSE;
    }

    private  IsinValidation(boolean checkCountryCode) {
        this.checkCountryCode = checkCountryCode;
    }

    public  boolean isValid(String code) {
        final boolean valid = VALIDATOR.isValid(code);
        if (valid && checkCountryCode) {
            return checkCode(code.substring(0,2));
        }
        return valid;
    }


    public  Object validate(String code) {
        final Object validate = VALIDATOR.validate(code);
        if (validate != null && checkCountryCode) {
            return checkCode(code.substring(0,2)) ? validate : null;
        }
        return validate;
    }

    private  boolean checkCode(String code) {
        return Arrays.binarySearch(CCODES, code) >= 0
        ;
    }

}





