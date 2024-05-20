package de.gameofpods.podcastproject.views.management;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Management")
@Route(value = "manage", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ManagementView extends MainLayoutPage {

    public ManagementView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        super(authenticatedUser, accessChecker);
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);

        Image img = new Image("images/empty-plant.png", "placeholder plant");
        img.setWidth("200px");
        layout.add(img);

        H2 header = new H2("This place intentionally left empty");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        layout.add(header);
        layout.add(new Paragraph("It’s a place where you can grow your own UI 🤗"));

        layout.setSizeFull();
        layout.setJustifyContentMode(VerticalLayout.JustifyContentMode.CENTER);
        layout.setDefaultHorizontalComponentAlignment(VerticalLayout.Alignment.CENTER);
        getStyle().set("text-align", "center");
        this.add(layout);
    }

}
