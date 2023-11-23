package de.gameofpods.podcastproject.views.podcast.podcasters;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("podcast/podcaster/:username")
@AnonymousAllowed
@Uses(Icon.class)
public class PodcasterView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var selectedUser = beforeEnterEvent.getRouteParameters().get("username").orElseThrow();
    }

}
