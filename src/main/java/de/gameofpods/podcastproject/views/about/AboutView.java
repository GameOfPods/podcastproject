package de.gameofpods.podcastproject.views.about;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.config.Libraries;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.i18n.Translations;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;

import java.util.ArrayList;
import java.util.List;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class AboutView extends MainLayoutPage {

    public AboutView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        super(authenticatedUser, accessChecker, true);

        var layout = new VerticalLayout();

        var titleName = "Podcast Project";
        if (Config.getConfig("pom.properties") != null) {
            var tmp = Config.getConfig("pom.properties").get("version");
            titleName += tmp != null && !tmp.toString().startsWith("@") ? "-v" + tmp : "";
        }
        var heading = new HorizontalLayout(
                new H1(Config.getConfig("application").get("name").toString()),
                new H2(Translations.getTranslation("#powered-by", authenticatedUser)),
                new H2(
                        new Anchor("https://github.com/GameOfPods/podcastproject", titleName)
                )
        );
        heading.setPadding(true);
        heading.setAlignItems(FlexComponent.Alignment.BASELINE);

        layout.add(heading);


        String about_txt = Translations.getTranslation("#about-placeholder", authenticatedUser);
        var about_cfg = Config.getConfig("about");
        if (about_cfg != null) {
            var locale = LanguageManager.getUserLocale(authenticatedUser.get().orElse(null));
            ArrayList<String> langCodes = new ArrayList<>();
            langCodes.add(locale.getISO3Language());
            langCodes.add(locale.getLanguage());
            langCodes.add("en");
            langCodes.add("eng");
            langCodes.addAll(about_cfg.keySet());
            var matchingKey = langCodes.stream().filter(p -> about_cfg.get(p) instanceof String && !about_cfg.get(p).toString().isEmpty()).findFirst();
            if (matchingKey.isPresent()) {
                about_txt = about_cfg.get(matchingKey.get()).toString();
            }
        }

        layout.add(new Paragraph(about_txt));

        //noinspection unchecked
        if (Config.getConfig("pom.properties") != null
                && Config.getConfig("pom.properties").get("libraries") != null
                && Config.getConfig("pom.properties").get("libraries") instanceof List
                && ((List) Config.getConfig("pom.properties").get("libraries")).stream().allMatch(l -> l instanceof Libraries)
        ) {

            layout.add(new H1(Translations.getTranslation("#libraries", authenticatedUser)));

            Grid<Libraries> libraryGrid = new Grid<>(Libraries.class, false);
            libraryGrid.addColumn(Libraries::getArtifactId).setHeader(Translations.getTranslation("#name", authenticatedUser)).setAutoWidth(false);
            libraryGrid.addColumn(libraries -> libraries.getVersion() == null ? "latest" : libraries.getVersion()).setHeader(Translations.getTranslation("#version", authenticatedUser)).setAutoWidth(false);
            libraryGrid.addColumn(libraries -> libraries.getLicense() == null ? "" : libraries.getLicense()).setHeader(Translations.getTranslation("#license", authenticatedUser)).setAutoWidth(false);
            libraryGrid.addColumn(
                    new ComponentRenderer<>((library) -> {
                        if (library.getURL() != null && !library.getURL().isEmpty()) {
                            return new Anchor(library.getURL(), library.getURL());
                        }
                        return null;
                    })).setHeader(Translations.getTranslation("#link", authenticatedUser)).setAutoWidth(true);

            //noinspection unchecked
            libraryGrid.setItems((List<Libraries>) Config.getConfig("pom.properties").get("libraries"));
            libraryGrid.setAllRowsVisible(true);
            layout.add(libraryGrid);
        }

        Div c = new Div(layout);
        setContent(c);
    }


}
