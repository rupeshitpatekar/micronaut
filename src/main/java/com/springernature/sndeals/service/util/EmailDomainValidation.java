package com.springernature.sndeals.service.util;

import org.apache.commons.lang3.StringUtils;

public class EmailDomainValidation
{
    public static boolean isValidEmailDomain(String email) {
        return !StringUtils.isEmpty(email) &&
            (email.endsWith("@springernature.com") || email.endsWith("@nature.com") || email.endsWith("@macmillaneducation.com"));
    }
}
