package de.gameofpods.podcastproject.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.gameofpods.podcastproject.i18n.Translations;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.legal.CookieManager;
import de.gameofpods.podcastproject.views.legal.Imprint;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MainLayoutPage extends Div {

    private final Div content;
    private final OrderedList footer;
    private final VerticalLayout layout;

    public MainLayoutPage(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this(authenticatedUser, accessChecker, true);
    }

    public MainLayoutPage(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker, boolean addDefaultFooter) {

        this.content = new Div();
        this.footer = new OrderedList();
        this.footer.addClassNames(
                LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL, LumoUtility.ListStyleType.NONE,
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Background.CONTRAST_10
        );
        this.layout = new VerticalLayout();

        this.setMinHeight(100, Unit.PERCENTAGE);
        this.setMinWidth(100, Unit.PERCENTAGE);

        this.footer.setWidth(100, Unit.PERCENTAGE);
        this.content.setSizeFull();

        layout.add(this.content, this.footer);
        layout.setFlexGrow(0, this.footer);
        layout.setFlexGrow(1, this.content);
        // layout.getStyle().set("flex", "1 1 1px");
        this.content.getStyle().set("flex", "1 1 1px");
        this.add(layout);
        var githublink = new Anchor("https://github.com/GameOfPods/podcastproject", "Powered by Podcast Project");
        githublink.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.XSMALL, LumoUtility.Height.MEDIUM, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.SMALL, LumoUtility.TextColor.PRIMARY);
        this.addFooterContent(githublink);
        if (addDefaultFooter) {
            for (MainLayout.MenuItemInfo menuItemInfo : defaultFooterItems(authenticatedUser, accessChecker)) {
                if (menuItemInfo.isVisible())
                    this.addFooterContent(menuItemInfo);
            }
        }
    }

    private static List<MainLayout.MenuItemInfo> defaultFooterItems(AuthenticatedUser a, AccessAnnotationChecker ac) {
        return new ArrayList<>() {{
            add(new MainLayout.MenuItemInfo(ac, Translations.getTranslation("#cookies", a), null, LumoUtility.TextColor.PRIMARY, CookieManager.class));
            if (Imprint.imprintPresent())
                add(new MainLayout.MenuItemInfo(ac, Translations.getTranslation("#imprint", a), null, LumoUtility.TextColor.PRIMARY, Imprint.class));
        }};
    }

    public void setContent(Component content) {
        this.content.removeAll();
        this.content.add(content);
    }

    public void clearFooter() {
        this.footer.removeAll();
    }

    public void addFooterContent(Component... components) {
        // this.footer.add(Arrays.stream(components).peek(c -> c.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST)).toList());
        this.footer.add(components);
    }

    @Override
    public void add(Component... components) {
        if (this.getComponentCount() > 0) {
            throw new NotImplementedException();
        }
        super.add(components);
    }

    @Override
    public void add(Collection<Component> components) {
        if (this.getComponentCount() > 0) {
            throw new NotImplementedException();
        }
        super.add(components);
    }

    @Override
    public void add(String text) {
        if (this.getComponentCount() > 0) {
            throw new NotImplementedException();
        }
        super.add(text);
    }

    @Override
    public void removeAll() {
        throw new NotImplementedException();
    }
}
