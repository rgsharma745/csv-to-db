package com.rgsharma745;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String normalize(final String input) {
        String whitespace = WHITESPACE.matcher(input).replaceAll("_");
        String normalized = Normalizer.normalize(whitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.replace("-", "_").toLowerCase(Locale.ENGLISH);
    }
}
