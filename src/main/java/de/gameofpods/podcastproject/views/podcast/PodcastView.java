package de.gameofpods.podcastproject.views.podcast;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.data.podcasts.PodcastEpisode;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;
import de.gameofpods.podcastproject.views.podcast.episode.PodcastEpisodeCard;
import de.gameofpods.podcastproject.views.podcast.episode.PodcastEpisodeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

@PageTitle("Podcast")
@Route(value = "podcast/:podcastID?", layout = MainLayout.class)
@RouteAlias(value = "/", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class PodcastView extends MainLayoutPage implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodcastView.class);

    private final Select<Podcast> availablePodcasts;
    private final AuthenticatedUser authenticatedUser;

    public PodcastView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessAnnotationChecker) {
        super(authenticatedUser, accessAnnotationChecker);
        /*Page page = UI.getCurrent().getPage();
        page.addBrowserWindowResizeListener(
                event -> Notification.show("Window width="
                        + event.getWidth()
                        + ", height=" + event.getHeight()));*/
        this.authenticatedUser = authenticatedUser;
        Optional<User> maybeUser = authenticatedUser.get();
        Locale userLocale = LanguageManager.getUserLocale(maybeUser.orElse(null));

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        //mainLayout.setWidthFull();
        mainLayout.addClassName(Gap.MEDIUM);

        availablePodcasts = new Select<>();
        availablePodcasts.setRenderer(new ComponentRenderer<Component, Podcast>(p -> {
            return PodcastCard.render(p, userLocale, false);
        }));

        var podcastInfo = new PodcastInformationView(false);
        podcastInfo.setWidth(400, Unit.PIXELS);
        mainLayout.add(podcastInfo);

        DataProvider<PodcastEpisode, Void> dataProvider =
                DataProvider.fromCallbacks(
                        // First callback fetches items based on a query
                        query -> {
                            if (availablePodcasts.getOptionalValue().isPresent()) {
                                int offset = query.getOffset();
                                int limit = query.getLimit();
                                return availablePodcasts.getValue().getEpisodes().stream().skip(offset).limit(limit);
                            } else {
                                return null;
                            }
                        },
                        // Second callback fetches the total number of items currently in the Grid.
                        // The grid can then use it to properly adjust the scrollbars.
                        query -> {
                            if (availablePodcasts.getOptionalValue().isPresent())
                                return availablePodcasts.getValue().getEpisodes().size();
                            return 0;
                        }
                );

        Grid<PodcastEpisode> podcastItems = new Grid<>();
        // TODO: Find better solution
        podcastItems.setAllRowsVisible(true);
        podcastItems.addComponentColumn(PodcastEpisodeCard::createCard);
        podcastItems.setHeight(100, Unit.PERCENTAGE);
        podcastItems.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        podcastItems.setDataProvider(dataProvider);
        podcastItems.setSizeFull();
        podcastItems.addItemClickListener((ComponentEventListener<ItemClickEvent<PodcastEpisode>>) podcastEpisodeItemClickEvent -> {
            var episode = podcastEpisodeItemClickEvent.getItem();
            UI.getCurrent().navigate(PodcastEpisodeView.class, new RouteParameters(new RouteParam("podcastID", episode.getPodcast().getPermanentID()), new RouteParam("episodeID", episode.getPermanentID())));
        });
        mainLayout.add(podcastItems);
        mainLayout.setFlexGrow(0, podcastInfo);
        mainLayout.setFlexGrow(1, podcastItems);

        availablePodcasts.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<Podcast>, Podcast>>) valueChangeEvent -> {
            // UI.getCurrent().navigate(PodcastView.class, new RouteParam("podcastID", valueChangeEvent.getValue().getPermanentID()));
            var p = valueChangeEvent.getValue();
            String route = RouteConfiguration.forSessionScope().getUrl(
                    PodcastView.class, new RouteParameters(new RouteParam("podcastID", p.getPermanentID()))
            );
            LOGGER.info("Selected \"" + p + "\" -> Set route to \"" + route + "\"");
            UI.getCurrent().getPage().getHistory().pushState(null, route);
            dataProvider.refreshAll();
            podcastInfo.setPodcast(p);
        });

        if (Podcast.getPodcasts().size() > 1) {
            var tmp = new VerticalLayout(availablePodcasts, mainLayout);
            tmp.setSizeFull();
            // tmp.setFlexGrow(0, availablePodcasts);
            // tmp.setFlexGrow(1, mainLayout);
            // tmp.setAlignItems(FlexComponent.Alignment.CENTER);
            this.setContent(tmp);
        } else {
            this.setContent(mainLayout);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        availablePodcasts.removeAll();
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
