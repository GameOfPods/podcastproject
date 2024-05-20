package de.gameofpods.podcastproject.i18n;

import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.utils.SimpleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Translations {
    private final static String LANGUAGE_TABLE_SEPARATOR = System.getenv().getOrDefault("LANGUAGE_TABLE_SEPARATOR", "\t");
    private final static Logger LOGGER = LoggerFactory.getLogger(Translations.class);
    private final static Map<String, TranslationEntity> TRANSLATION_ENTITY_MAP = new HashMap<>();

    static {
        var t1 = LocalDateTime.now();
        try {
            var baseTranslation = new String(
                    Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("i18n/translations.csv")).readAllBytes(),
                    StandardCharsets.UTF_8
            );
            var lines = baseTranslation.lines().toList();
            var languageKeys = Arrays.stream(lines.getFirst().split(LANGUAGE_TABLE_SEPARATOR)).skip(1).map(Locale::forLanguageTag).toList();
            var t = lines.stream().skip(1).map(l -> {
                var splitLine = l.split(LANGUAGE_TABLE_SEPARATOR);
                var key = splitLine[0];
                var ret = new TranslationEntity(key);
                for (int i = 1; i < Math.min(splitLine.length, languageKeys.size() + 1); i++) {
                    ret.addTranslation(languageKeys.get(i - 1), splitLine[i]);
                }
                return ret;
            }).collect(
                    () -> new HashMap<String, TranslationEntity>(),
                    (hm, te) -> hm.put(te.getKey(), te),
                    (hm1, hm2) -> hm1.putAll(hm2)
            );
            TRANSLATION_ENTITY_MAP.putAll(t);
        } catch (IOException | NullPointerException e) {
            LOGGER.error("base translation file not present or readable in resources at i18n/translations.csv", e);
        }
        var t2 = LocalDateTime.now();
        LOGGER.info("Finished loading translations in {}. Found {} unique keys.", SimpleUtils.formatTime(t1.until(t2, ChronoUnit.SECONDS)), TRANSLATION_ENTITY_MAP.size());
    }

    private Translations() {
    }

    public static String getTranslation(String key, Locale... locales) {
        var t = TRANSLATION_ENTITY_MAP.get(key);
        if (t == null)
            return key;
        return t.getTranslation(locales);
    }

    public static String getTranslation(String key, AuthenticatedUser authenticatedUser) {
        return getTranslation(key, authenticatedUser.get().orElse(null));
    }

    public static String getTranslation(String key, User user) {
        return getTranslation(key, LanguageManager.getUserLocale(user));
    }

    public static class TranslationEntity {
        private final String key;
        private final Map<String, String> translations = new HashMap<>();

        public TranslationEntity(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void addTranslation(Locale locale, String translation) {
            this.translations.put(locale.getISO3Language(), translation);
        }

        public String getTranslation(Locale... locale) {
            for (Locale l : locale) {
                if (this.translations.containsKey(l.getISO3Language())) {
                    return this.translations.get(l.getISO3Language());
                }
            }
            return this.getKey();
        }
    }

}
