package de.gameofpods.podcastproject.utils;

import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.i18n.Translations;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SimpleUtils {

    public static byte[] getResource(String resource) {
        InputStream input = null;
        try {
            input = Translations.class.getResourceAsStream("/resources/" + resource);
            if (input == null)
                input = Translations.class.getClassLoader().getResourceAsStream(resource);
            Objects.requireNonNull(input);
            return Objects.requireNonNull(input).readAllBytes();
        } catch (Exception ignored) {
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException ignored) {
                }
        }
        return null;
    }


    public static String encodeValueForURL(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String decodeValueForURL(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public static List<String> StringSplit(String text, String delimiter) {
        if (text.isEmpty())
            return Collections.emptyList();
        var idx = text.indexOf(delimiter);
        if (idx < 0)
            return Collections.singletonList(text);
        var ret = new ArrayList<String>() {{
            add(text.substring(0, idx));
        }};
        ret.addAll(StringSplit(text.substring(idx + delimiter.length()), delimiter));
        return ret;
    }

    public static String hash(byte[] bytes, String algorithm) {
        try {
            var md = MessageDigest.getInstance(algorithm);
            md.reset();
            md.update(bytes);
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatTime(long seconds) {
        return formatTime(seconds, null);
    }

    public static String formatTime(long seconds, User user) {
        Duration duration = Duration.ofSeconds(seconds);
        String ret = "";
        if (duration.isZero())
            return "00:00:00";
        else if (duration.isNegative())
            ret = "-";

        long t = duration.toDays();
        if (t > 0)
            ret += t + Translations.getTranslation("#day-one-char", user) + " ";
        duration = duration.minus(t, ChronoUnit.DAYS);

        if (duration.isZero())
            return ret;

        t = duration.toHours();
        ret += String.format("%02d:", t);
        duration = duration.minus(t, ChronoUnit.HOURS);

        t = duration.toMinutes();
        ret += String.format("%02d:", t);
        duration = duration.minus(t, ChronoUnit.MINUTES);

        t = duration.toSeconds();
        ret += String.format("%02d", t);
        return ret;
    }

}
