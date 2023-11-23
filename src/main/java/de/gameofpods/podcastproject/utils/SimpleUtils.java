package de.gameofpods.podcastproject.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SimpleUtils {
    public static String encodeValueForURL(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String decodeValueForURL(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
