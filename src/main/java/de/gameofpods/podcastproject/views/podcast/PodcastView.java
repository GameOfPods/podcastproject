package de.gameofpods.podcastproject.views.podcast;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.podcast.episode.PodcastEpisodeCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Podcast")
@Route(value = "podcast/:podcastID?", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class PodcastView extends Div implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodcastView.class);

    private final Select<Podcast> availablePodcasts;
    private final OrderedList podcastEpisodeList;
    private final AuthenticatedUser authenticatedUser;

    public PodcastView(AuthenticatedUser authenticatedUser) {

        /*Page page = UI.getCurrent().getPage();
        page.addBrowserWindowResizeListener(
                event -> Notification.show("Window width="
                        + event.getWidth()
                        + ", height=" + event.getHeight()));*/

        this.authenticatedUser = authenticatedUser;
        Optional<User> maybeUser = authenticatedUser.get();
        Locale userLocale = LanguageManager.getUserLocale(maybeUser.orElse(null));

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        //mainLayout.setSizeFull();
        mainLayout.setWidthFull();
        mainLayout.addClassName(Gap.MEDIUM);

        availablePodcasts = new Select<>();
        mainLayout.add(availablePodcasts);
        mainLayout.setFlexGrow(0., availablePodcasts);
        availablePodcasts.setRenderer(new ComponentRenderer<Component, Podcast>(p -> {
            return PodcastCard.render(p, userLocale, false);
        }));

        VerticalLayout podcastItems = new VerticalLayout();
        podcastEpisodeList = new OrderedList();
        podcastItems.setHeightFull();
        podcastItems.setWidth(null);
        podcastItems.add(podcastEpisodeList);
        mainLayout.setFlexGrow(1., podcastItems);

        mainLayout.add(availablePodcasts);

        Scroller scroller = new Scroller(podcastItems);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "var(--lumo-space-m)");
        mainLayout.add(scroller);

        availablePodcasts.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<Podcast>, Podcast>>) valueChangeEvent -> {
            // UI.getCurrent().navigate(PodcastView.class, new RouteParam("podcastID", valueChangeEvent.getValue().getPermanentID()));
            var p = valueChangeEvent.getValue();
            String route = RouteConfiguration.forSessionScope().getUrl(
                    PodcastView.class, new RouteParameters(new RouteParam("podcastID", p.getPermanentID()))
            );
            LOGGER.info("Selected \"" + p + "\" -> Set route to \"" + route + "\"");
            UI.getCurrent().getPage().getHistory().pushState(null, route);
            podcastEpisodeList.removeAll();
            podcastEpisodeList.add(p.getEpisodes().stream().map(pe -> PodcastEpisodeCard.createCard(p, pe)).collect(Collectors.toList()));
        });

        add(mainLayout);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        availablePodcasts.removeAll();
        podcastEpisodeList.removeAll();
        Optional<User> maybeUser = authenticatedUser.get();
        Locale userLocale = LanguageManager.getUserLocale(maybeUser.orElse(null));
        var selectedPodcast = beforeEnterEvent.getRouteParameters().get("podcastID").orElse(null);
        boolean oneFound = false;
        var pods = Podcast.getPodcasts().stream().sorted().toList();
        availablePodcasts.setItems(pods);
        availablePodcasts.setValue(Podcast.getPodcastByPermanentID(selectedPodcast, pods.getFirst()));
//        for (Podcast podcast : PODCASTS) {
//            var s = podcast.getPermanentID().equalsIgnoreCase(selectedPodcast);
//            if (s && oneFound)
//                throw new RuntimeException("Already found a podcast with ID " + selectedPodcast);
//            // availablePodcasts.add(PodcastCard.render(podcast, userLocale, false));
//            // availablePodcasts.addClickListener(ce -> UI.getCurrent().navigate(PodcastView.class, new RouteParam("podcastID", podcast.getPermanentID())));
//            if (s){
//                oneFound = true;
//                podcastEpisodeList.add(podcast.getEpisodes().stream().map(
//                        e -> PodcastEpisodeCard.createCard(podcast, e)
//                ).collect(Collectors.toList()));
//            }
//        }
    }
}
