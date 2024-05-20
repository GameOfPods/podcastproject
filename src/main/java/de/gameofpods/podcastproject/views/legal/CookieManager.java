package de.gameofpods.podcastproject.views.legal;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.cookiemanagement.Cookies;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.i18n.Translations;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static de.gameofpods.podcastproject.utils.SimpleUtils.formatTime;

@PageTitle("Cookies")
@Route(value = "cookies", layout = MainLayout.class)
@RouteAlias(value = "kekse", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class CookieManager extends MainLayoutPage {

    private final static Logger LOGGER = LoggerFactory.getLogger(CookieManager.class);
    private static final Renderer RENDERER = HtmlRenderer.builder().sanitizeUrls(true).build();
    private static final Parser PARSER = Parser.builder().build();

    public CookieManager(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        super(authenticatedUser, accessChecker);

        var layout = new VerticalLayout();
        layout.add(new H1(Translations.getTranslation("#cookie-management-heading", authenticatedUser)));

        var cookieConfig = Config.getConfig("cookies");
        if (cookieConfig != null) {
            var langCodes = new ArrayList<String>();
            var locale = LanguageManager.getUserLocale(authenticatedUser.get().orElse(null));
            langCodes.add(locale.getISO3Language());
            langCodes.add(locale.getLanguage());
            langCodes.add("en");
            langCodes.add("eng");
            langCodes.addAll(cookieConfig.keySet());
            var matchingKey = langCodes.stream().filter(p -> cookieConfig.get(p) instanceof String && !cookieConfig.get(p).toString().isEmpty()).findFirst();
            if (matchingKey.isEmpty()) {
                LOGGER.error("No cookie text in cookies config that is usable. Please check defined imprints against documentation");
            } else {
                Node document = PARSER.parse(cookieConfig.get(matchingKey.get()).toString());
                layout.add(new Html("<text>" + RENDERER.render(document) + "</text>"));
            }
        }

        layout.add(new H2(Translations.getTranslation("#used-cookies", authenticatedUser)));
        layout.add(new Text(Translations.getTranslation("#used-cookies-description", authenticatedUser)));
        for (Cookies.NECESSETY usedNecessity : Cookies.NECESSETY.usedNecessities()) {
            var explainGrid = new Grid<Cookies>();
            explainGrid.setItems(Cookies.getCookiesByNecessity(usedNecessity));
            explainGrid.addColumn(Cookies::getCookieKey).setHeader(Translations.getTranslation("#key", authenticatedUser)).setAutoWidth(true);
            explainGrid.addColumn(c -> {
                var d = c.getDuration();
                if (d > 0)
                    return formatTime(d);
                return Translations.getTranslation("#session", authenticatedUser);
            }).setHeader(Translations.getTranslation("#max-age", authenticatedUser)).setAutoWidth(true);
            explainGrid.addColumn(Cookies::getFunction).setHeader(Translations.getTranslation("#function", authenticatedUser)).setAutoWidth(true);
            explainGrid.setAllRowsVisible(true);
            layout.add(new H4(usedNecessity.name() + "-Cookies"), explainGrid);
        }

        layout.add(new H2(Translations.getTranslation("#cookies-on-off", authenticatedUser)));
        var enabledCookies = new CheckboxGroup<Cookies.NECESSETY>();
        enabledCookies.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        enabledCookies.setItemEnabledProvider((SerializablePredicate<Cookies.NECESSETY>) necessety -> necessety != Cookies.NECESSETY.FUNCTIONAL);
        enabledCookies.setItems(Cookies.NECESSETY.usedNecessities());
        var userAllowedCookies = de.gameofpods.podcastproject.cookiemanagement.CookieManager.getAllowedCookies();
        if (userAllowedCookies == null || userAllowedCookies.isEmpty()) {
            enabledCookies.select(Cookies.NECESSETY.FUNCTIONAL);
        } else {
            enabledCookies.select(userAllowedCookies);
        }
        enabledCookies.addSelectionListener(new MultiSelectionListener<CheckboxGroup<Cookies.NECESSETY>, Cookies.NECESSETY>() {
            @Override
            public void selectionChange(MultiSelectionEvent<CheckboxGroup<Cookies.NECESSETY>, Cookies.NECESSETY> multiSelectionEvent) {
                de.gameofpods.podcastproject.cookiemanagement.CookieManager.setUserAllowedCookies(enabledCookies.getSelectedItems());
            }
        });
        layout.add(enabledCookies);


        Div c = new Div(layout);
        // c.setMaxWidth(1000, Unit.PIXELS);

        setContent(c);

    }
}
