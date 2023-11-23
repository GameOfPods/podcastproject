package de.gameofpods.podcastproject.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.about.AboutView;
import de.gameofpods.podcastproject.views.management.ManagementView;
import de.gameofpods.podcastproject.views.podcast.PodcastView;
import de.gameofpods.podcastproject.views.wiki.WikiView;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL,
                    TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

    }

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        addToNavbar(true, createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        Div layout = new Div();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);

        Optional<User> maybeUser = authenticatedUser.get();
        var potLocales = LanguageManager.languages();
        var userLocale = LanguageManager.matchLocaleToExisting(
                LanguageManager.getUserLocale(maybeUser.orElse(null)),
                potLocales
        );

        var languageSelector = new Select<Locale>();
        languageSelector.setWidth(70, Unit.PIXELS);
        // languageSelector.setLabel("Languages");
        languageSelector.setItems(potLocales);
        languageSelector.setValue(userLocale);
        languageSelector.setItemLabelGenerator(l -> LanguageManager.localeToEmoji(l));
        languageSelector.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<Locale>, Locale>>) selectLocaleComponentValueChangeEvent -> {
            var l = selectLocaleComponentValueChangeEvent.getValue();
            if (l == null)
                return;
            LanguageManager.setUserLocale(maybeUser.orElse(null), l);
            try {
                UI.getCurrent().getPage().reload();
            }catch (NullPointerException ignored){
            }
        });


        H1 appName = new H1(Config.getConfig("application").get("name").toString());
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
        layout.add(appName, languageSelector);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }

        header.add(layout, nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("About", LineAwesomeIcon.GLOBE_SOLID.create(), AboutView.class), //

                new MenuItemInfo("Podcast", LineAwesomeIcon.HEADPHONES_SOLID.create(), PodcastView.class), //

                new MenuItemInfo("Wiki", LineAwesomeIcon.INFO_SOLID.create(), WikiView.class), //

                new MenuItemInfo("Management", LineAwesomeIcon.COG_SOLID.create(), ManagementView.class), //

        };
    }

}
