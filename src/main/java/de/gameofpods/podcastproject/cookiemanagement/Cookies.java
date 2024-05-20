package de.gameofpods.podcastproject.cookiemanagement;

import de.gameofpods.podcastproject.utils.SimpleUtils;

import java.util.Arrays;
import java.util.Collection;

public enum Cookies {

    _SESSIONID("JSESSIONID", -1, "Session id used by vaadin backend", NECESSETY.FUNCTIONAL),
    LANGUAGE("UserLanguage", 60 * 24 * 60 * 60, "Saves user language settings", NECESSETY.FUNCTIONAL),
    COOKIE_CONSENT("PodcastProjectCookieConsent-" + SimpleUtils.hash(String.join("", Arrays.stream(NECESSETY.values()).map(Enum::name).sorted().toList()).getBytes(), "MD5"), 60 * 24 * 60 * 60, "Saves user language settings", NECESSETY.FUNCTIONAL);

    private final String cookieKey, function;
    private final int duration;
    private final NECESSETY necessety;

    Cookies(String cookieKey, int duration, String function, NECESSETY necessety) {
        this.cookieKey = cookieKey;
        this.duration = duration;
        this.function = function;
        this.necessety = necessety;
    }

    public static Collection<Cookies> getCookiesByNecessity(NECESSETY necessety) {
        return Arrays.stream(Cookies.values()).filter(c -> c.necessety == necessety).sorted().toList();
    }

    public String getCookieKey() {
        return cookieKey;
    }

    public String getFunction() {
        return function;
    }

    public int getDuration() {
        return duration;
    }

    public NECESSETY getNecessety() {
        return necessety;
    }

    public enum NECESSETY {
        FUNCTIONAL,
        OPTIONAL;

        public static Collection<NECESSETY> usedNecessities() {
            return Arrays.stream(Cookies.values()).map(Cookies::getNecessety).distinct().sorted().toList();
        }

    }

}
