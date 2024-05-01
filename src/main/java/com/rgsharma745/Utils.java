package com.rgsharma745;

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String normalize(String input) {
        String whitespace = WHITESPACE.matcher(input).replaceAll("_");
        String normalized = Normalizer.normalize(whitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.replace("-","_").toLowerCase(Locale.ENGLISH);
    }
}
