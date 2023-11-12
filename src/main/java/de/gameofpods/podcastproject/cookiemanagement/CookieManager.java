package de.gameofpods.podcastproject.cookiemanagement;

import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.andreinc.aleph.AlephFormatter.str;

public class CookieManager {

    private static final Logger LOGGER = LogManager.getLogger(CookieManager.class);

    public static String getCookieValue(String key, String def, boolean createIfNotExist){
        var cookie = getCookieByName(key);
        if (cookie != null){
            return cookie.getValue();
        }
        if (def != null && createIfNotExist)
            setCookieValue(key, def);
        return def;
    }

    public static void setCookieValue(String key, String value, String path){
        LOGGER.info(str("Setting cookie #{key} to #{value} @ #{path}")
                .args("key", key).arg("value", value).arg("path", path).fmt()
        );
        Cookie myCookie = new Cookie(key, value);
        myCookie.setPath(value);
        VaadinService.getCurrentResponse().addCookie(myCookie);
    }

    public static void setCookieValue(String key, String value){
        CookieManager.setCookieValue(key, value, VaadinService.getCurrentRequest().getContextPath());
    }

    private static Cookie getCookieByName(String name) {
        // Fetch all cookies from the request
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }
}
