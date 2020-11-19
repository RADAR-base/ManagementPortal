package org.radarcns.management.web.rest.util;

import org.apache.commons.lang.StringUtils;

public class FilterUtil {
    public static boolean isValid(String str) {
        return StringUtils.isNotBlank(str) && !str.equals("null");
    }
}
