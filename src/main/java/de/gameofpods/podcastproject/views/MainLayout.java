package de.gameofpods.podcastproject.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.cookiemanagement.CookieManager;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.i18n.Translations;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.about.AboutView;
import de.gameofpods.podcastproject.views.login.LoginView;
import de.gameofpods.podcastproject.views.management.ManagementView;
import de.gameofpods.podcastproject.views.podcast.PodcastView;
import org.apache.commons.lang3.tuple.Pair;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        addToNavbar(false, createHeaderContent());

        if (!CookieManager.cookieConsentGiven()) {

            Dialog cookieDialog = new Dialog("Cookie Confirmation");
            cookieDialog.setWidth(100, Unit.PERCENTAGE);
            cookieDialog.setModal(true);
            cookieDialog.setCloseOnEsc(false);
            cookieDialog.setCloseOnOutsideClick(false);
            //TODO: Search option to move Dialog
            cookieDialog.getElement().getStyle().setBottom("0");
            cookieDialog.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            var cookieLayout = CookieManager.cookieSettingsComponent(() -> {
                cookieDialog.close();
                UI.getCurrent().getPage().reload();
            });
            cookieLayout.setSizeFull();
            cookieDialog.add(cookieLayout);
            cookieDialog.open();
        }
    }

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

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
        languageSelector.setItemLabelGenerator(LanguageManager::localeToEmoji);
        languageSelector.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<Locale>, Locale>>) selectLocaleComponentValueChangeEvent -> {
            var l = selectLocaleComponentValueChangeEvent.getValue();
            if (l == null)
                return;
            LanguageManager.setUserLocale(maybeUser.orElse(null), l);
            try {
                UI.getCurrent().getPage().reload();
            } catch (NullPointerException ignored) {
            }
        });


        H1 appName = new H1(Config.getConfig("application").get("name").toString());
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
        layout.addClassNames(Padding.Vertical.MEDIUM);
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
            userName.getSubMenu().addItem(Translations.getTranslation("#sign-out", authenticatedUser), e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            var loginLink = new RouterLink(Translations.getTranslation("#sign-in", authenticatedUser), LoginView.class);
            // Anchor loginLink = new Anchor("login", Translations.getTranslation("#sign-in"));
            layout.add(loginLink);
        }

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        OrderedList list = new OrderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems(authenticatedUser, accessChecker)) {
            if (menuItem.isVisible()) {
                list.add(menuItem);
            }

        }

        header.add(layout, nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems(AuthenticatedUser a, AccessAnnotationChecker ac) {
        return new MenuItemInfo[]{ //
                new MenuItemInfo(ac, Translations.getTranslation("#podcast", a), LineAwesomeIcon.HEADPHONES_SOLID.create(), null, PodcastView.class), //
                // new MenuItemInfo(ac, Translations.getTranslation("#wiki", a), LineAwesomeIcon.INFO_SOLID.create(), null, WikiView.class), //
                new MenuItemInfo(ac, Translations.getTranslation("#management", a), LineAwesomeIcon.COG_SOLID.create(), null, ManagementView.class), //
                new MenuItemInfo(ac, Translations.getTranslation("#about", a), LineAwesomeIcon.INFO_CIRCLE_SOLID.create(), null, AboutView.class),
        };
    }

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private boolean visible = false;

        public MenuItemInfo(AccessAnnotationChecker accessAnnotationChecker, String menuTitle, Component icon, String textColor, Class<? extends Component> view) {
            this.render(accessAnnotationChecker, menuTitle, icon, textColor, Pair.of("", view));
        }

        @SafeVarargs
        public MenuItemInfo(AccessAnnotationChecker accessAnnotationChecker, String menuTitle, Component icon, String textColor, Pair<String, Class<? extends Component>>... views) {
            this.render(accessAnnotationChecker, menuTitle, icon, textColor, views);
        }

        private void render(AccessAnnotationChecker accessAnnotationChecker, String menuTitle, Component icon, String textColor, Pair<String, Class<? extends Component>>... views) {
            String[] classNames = {
                    Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL, textColor == null ? TextColor.BODY : textColor
            };
            String[] classNamesText = {
                    FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP
            };
            var filteredList = Arrays.stream(views).filter(stringClassPair -> accessAnnotationChecker.hasAccess(stringClassPair.getRight())).toList();
            if (filteredList.isEmpty()) {
                visible = false;
            } else if (filteredList.size() == 1) {
                RouterLink link = new RouterLink();
                // Use Lumo classnames for various styling
                link.addClassNames(classNames);
                link.setRoute(views[0].getValue());

                Span text = new Span(menuTitle);
                //text.addClassNames(textColor == null ? TextColor.BODY : textColor);
                // Use Lumo classnames for various styling
                text.addClassNames(classNamesText);

                if (icon != null) {
                    link.add(icon);
                }
                link.add(text);
                add(link);
                visible = true;
            } else {
                Select<Pair<String, Class<? extends Component>>> select = new Select<>();
                select.addValueChangeListener(selectPairComponentValueChangeEvent -> UI.getCurrent().navigate(selectPairComponentValueChangeEvent.getValue().getValue()));
                select.setItemLabelGenerator(Pair::getLeft);
                select.setItems(filteredList);
                select.setEmptySelectionCaption(menuTitle);
                select.setEmptySelectionAllowed(false);
                select.addClassNames(textColor == null ? TextColor.BODY : textColor);
                add(select);
                visible = true;
//                Span title = new Span(menuTitle);
//                title.addClassNames(classNamesText);
//                VerticalLayout layout = new VerticalLayout();
//                layout.addClassName(Gap.SMALL);
//                PaperMenuButton btn = new PaperMenuButton(title, layout);
////                Details dropdown = new Details(menuTitle);
////                dropdown.addClassNames(classNames);
////                dropdown.addClassNames(classNamesText);
//                for (Pair<String, Class<? extends Component>> stringClassPair : filteredList) {
//                    RouterLink link = new RouterLink();
//                    link.setRoute(stringClassPair.getValue());
//                    link.addClassNames(classNames);
//                    Span text = new Span(stringClassPair.getKey());
//                    text.addClassNames(classNamesText);
//                    link.add(text);
//                    layout.add(link);
//                }
//                visible = true;
//                add(select);
            }
        }

        @Override
        public boolean isVisible() {
            return visible;
        }
    }

}
