package de.gameofpods.podcastproject.cookiemanagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static de.gameofpods.podcastproject.utils.SimpleUtils.StringSplit;
import static net.andreinc.aleph.AlephFormatter.str;

public class CookieManager {

    private static final String COOKIE_ARRAY_SEPARATOR = "|";

    private static final Logger LOGGER = LogManager.getLogger(CookieManager.class);

    public static String getCookieValue(Cookies cookie, String def) {
        var c = getCookieByName(cookie.getCookieKey());
        if (c != null) {
            return c.getValue();
        }
        if (def != null)
            setCookieValue(cookie, def);
        return def;
    }

    public static void setCookieValue(Cookies cookie, String value, String path) {
        try {
            if (cookie.getNecessety() != Cookies.NECESSETY.FUNCTIONAL && !Objects.requireNonNull(getAllowedCookies()).contains(cookie.getNecessety()))
                return;
        } catch (NullPointerException e) {
            return;
        }

        LOGGER.info(str("Setting cookie #{key} to #{value} @ #{path}")
                .args("key", cookie.getCookieKey()).arg("value", value).arg("path", path).fmt()
        );
        Cookie myCookie = new Cookie(cookie.getCookieKey(), value);
        myCookie.setPath(value);
        myCookie.setMaxAge(cookie.getDuration());
        VaadinService.getCurrentResponse().addCookie(myCookie);
    }

    public static void setCookieValue(Cookies cookie, String value) {
        CookieManager.setCookieValue(cookie, value, VaadinService.getCurrentRequest().getContextPath());
    }

    private static Cookie getCookieByName(String name) {
        // Fetch all cookies from the request
        ArrayList<Cookie> cookies = new ArrayList<>();
        if (System.getenv("SEARCH_RESPONSE_FOR_COOKIES") != null) {
            var tmp = getCookiesFromResponse(VaadinService.getCurrentResponse());
            if (tmp != null)
                cookies = tmp;
        }
        cookies.addAll(Arrays.stream(VaadinService.getCurrentRequest().getCookies()).toList());

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    public static Set<Cookies.NECESSETY> getAllowedCookies() {
        var allowedCookies = getCookieValue(Cookies.COOKIE_CONSENT, null);
        if (allowedCookies == null || allowedCookies.isEmpty())
            return null;
        try {
            return StringSplit(allowedCookies, COOKIE_ARRAY_SEPARATOR).stream().map(Cookies.NECESSETY::valueOf).collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean cookieConsentGiven() {
        return getAllowedCookies() != null;
    }

    public static Div cookieSettingsComponent(Runnable onClick) {
        var cookieLayout = new FlexLayout();
        var buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName(LumoUtility.Gap.MEDIUM);
        cookieLayout.add(
                new HorizontalLayout(
                        new Span("This website uses technical and optional cookies for the user experience"),
                        new RouterLink("Cookies", de.gameofpods.podcastproject.views.legal.CookieManager.class)
                )
        );

        buttonLayout.add(new Button("All cookies", LineAwesomeIcon.COOKIE_SOLID.create(), event -> {
            setUserAllowedCookies(Arrays.stream(Cookies.NECESSETY.values()).toList());
            onClick.run();
        }));


        CheckboxGroup<Cookies.NECESSETY> allowedCookies = new CheckboxGroup<>();
        allowedCookies.setItems(Cookies.NECESSETY.usedNecessities());
        var userAllowedCookies = getAllowedCookies();
        if (userAllowedCookies == null || userAllowedCookies.isEmpty()) {
            allowedCookies.select(Cookies.NECESSETY.FUNCTIONAL);
        } else {
            allowedCookies.select(userAllowedCookies);
        }
        allowedCookies.setItemEnabledProvider((SerializablePredicate<Cookies.NECESSETY>) necessety -> necessety != Cookies.NECESSETY.FUNCTIONAL);

        buttonLayout.add(allowedCookies);
        buttonLayout.add(new Button("Only selected", LineAwesomeIcon.COOKIE_BITE_SOLID.create(), event -> {
            setUserAllowedCookies(allowedCookies.getSelectedItems());
            onClick.run();
        }));
        cookieLayout.add(buttonLayout);

        cookieLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cookieLayout.setAlignContent(FlexLayout.ContentAlignment.CENTER);
        cookieLayout.getStyle().set("align-items", "center");
        cookieLayout.setSizeFull();
        return new Div(cookieLayout);
    }

    public static void setUserAllowedCookies(Collection<Cookies.NECESSETY> allowedCookies) {
        setCookieValue(
                Cookies.COOKIE_CONSENT,
                String.join(COOKIE_ARRAY_SEPARATOR, allowedCookies.stream().map(Cookies.NECESSETY::toString).collect(Collectors.toSet()))
        );
    }

    private static ArrayList<Cookie> getCookiesFromResponse(VaadinResponse origResponse) {
        Object response = origResponse;
        Object cookieList = null;
        while (true) {
            try {
                var method = response.getClass().getMethod("getCookies");
                cookieList = method.invoke(response);
                break;
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException |
                     InvocationTargetException ignore) {
            }
            try {
                cookieList = response.getClass().getField("cookies").get(response);
                break;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            response = traverseResponse(response);
            if (response == null)
                break;
        }
        if (cookieList != null) {
            // noinspection unchecked
            return (ArrayList<Cookie>) cookieList;
        }
        return null;
    }

    private static Object traverseResponse(Object response) {
        try {
            var method = response.getClass().getMethod("getResponse");
            return method.invoke(response);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException |
                 InvocationTargetException ignore) {
        }
        try {
            Field field = org.springframework.util.ReflectionUtils.findField(response.getClass(), "response");
            org.springframework.util.ReflectionUtils.makeAccessible(Objects.requireNonNull(field));
            return field.get(response);
        } catch (IllegalAccessException | NullPointerException ignored) {
        }
        return null;
    }

}
