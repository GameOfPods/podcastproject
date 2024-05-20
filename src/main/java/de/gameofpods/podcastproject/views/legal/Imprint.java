package de.gameofpods.podcastproject.views.legal;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.i18n.LanguageManager;
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

@PageTitle("Podcast")
@Route(value = "imprint", layout = MainLayout.class)
@RouteAlias(value = "impressum", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class Imprint extends MainLayoutPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(Imprint.class);
    private static final Renderer RENDERER = HtmlRenderer.builder().sanitizeUrls(true).build();
    private static final Parser PARSER = Parser.builder().build();

    public Imprint(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        super(authenticatedUser, accessChecker);
        var imprints = Config.getConfig("imprint");
        if (imprints == null) {
            LOGGER.error("No imprint config specified. See documentation on how to create one");
            setContent(new H1("No imprint specified"));
            return;
        }
        var langCodes = new ArrayList<String>();
        var locale = LanguageManager.getUserLocale(authenticatedUser.get().orElse(null));
        langCodes.add(locale.getISO3Language());
        langCodes.add(locale.getLanguage());
        langCodes.add("en");
        langCodes.add("eng");
        langCodes.addAll(imprints.keySet());
        var matchingKey = langCodes.stream().filter(p -> imprints.get(p) instanceof String && !imprints.get(p).toString().isEmpty()).findFirst();
        if (matchingKey.isEmpty()) {
            LOGGER.error("No imprint in imprint config that is usable. Please check defined imprints against documentation");
            setContent(new H1("No imprint specified"));
            return;
        }

        Node document = PARSER.parse(imprints.get(matchingKey.get()).toString());
        setContent(new Html("<text>" + RENDERER.render(document) + "</text>"));
    }

    public static boolean imprintPresent() {
        var map = Config.getConfig("imprint");
        if (map == null)
            return false;
        if (map.keySet().isEmpty())
            return false;
        return map.keySet().stream().map(map::get).anyMatch(p -> p instanceof String && !p.toString().isEmpty());
    }

}
