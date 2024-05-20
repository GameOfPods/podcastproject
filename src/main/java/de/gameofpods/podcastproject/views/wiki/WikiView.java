package de.gameofpods.podcastproject.views.wiki;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;

@PageTitle("Wiki")
@Route(value = "wiki", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class WikiView extends MainLayoutPage {

    public WikiView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        super(authenticatedUser, accessChecker);
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout layoutRow = new HorizontalLayout();
        H1 h1 = new H1();
        TextField textField = new TextField();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        layout.setHeightFull();
        layout.setWidthFull();
        layoutRow.setWidthFull();
        layoutRow.addClassName(Gap.MEDIUM);
        h1.setText("Heading");
        layoutRow.setFlexGrow(1.0, h1);
        textField.setLabel("Search");
        layout.setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidthFull();
        layout.add(layoutRow);
        layoutRow.add(h1);
        layoutRow.add(textField);
        layout.add(layoutColumn2);
        this.setContent(layout);
    }
}
