package de.gameofpods.podcastproject.i18n;

import com.vaadin.flow.server.VaadinService;
import de.gameofpods.podcastproject.cookiemanagement.CookieManager;
import de.gameofpods.podcastproject.cookiemanagement.Cookies;
import de.gameofpods.podcastproject.data.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageManager {

    private static final Logger LOGGER = LogManager.getLogger(LanguageManager.class);

    public static Locale getUserLocale(User user) {
        if (user != null) {
            // TODO: Get language from user object
        }
        var cookie_lang = CookieManager.getCookieValue(Cookies.LANGUAGE, VaadinService.getCurrentRequest().getLocale().toLanguageTag());
        return Locale.forLanguageTag(cookie_lang);
    }

    public static void setUserLocale(User user, Locale locale){
        if (user != null){
            // TODO: Set language in user object
        }
        CookieManager.setCookieValue(Cookies.LANGUAGE, locale.toLanguageTag());
    }

    public static List<Locale> languages() {
        return new ArrayList<>() {{
            add(Locale.GERMANY);
            add(Locale.UK);
        }};
    }

    public static Locale matchLocaleToExisting(Locale expected, List<Locale> given) {
        if (!given.contains(expected)) {
            var _t = expected.getISO3Language();
            var l = given.stream()
                    .filter(_l -> _l.getISO3Language().equals(_t))
                    .toList();
            if (l.isEmpty())
                expected = given.getFirst();
            else
                expected = l.getFirst();
        }
        return expected;
    }

    public static String localeToEmoji(Locale locale) {
        final var countryCode = locale.getCountry();
        LOGGER.debug("Emoji for {}", countryCode);
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }
}
