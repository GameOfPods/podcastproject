package de.gameofpods.podcastproject.views.podcast.episode;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.gameofpods.podcastproject.components.audioplayer.AudioPlayer;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;


@Route(value = "podcast/episode/:podcastID/:episodeID", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class PodcastEpisodeView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodcastEpisodeView.class);
    private final AuthenticatedUser authenticatedUser;
    private String title = "Episode";

    public PodcastEpisodeView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        Optional<User> maybeUser = authenticatedUser.get();
        Locale userLocale = LanguageManager.getUserLocale(maybeUser.orElse(null));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var selectedPodcastID = beforeEnterEvent.getRouteParameters().get("podcastID").orElseThrow();
        var selectedEpisodeID = beforeEnterEvent.getRouteParameters().get("episodeID").orElseThrow();
        Podcast selectedPodcast = Podcast.getPodcastByPermanentID(selectedPodcastID, null);
        if (selectedPodcast == null)
            throw new RuntimeException("");
        var selectedEpisode = selectedPodcast.getEpisodes().stream().filter(pe -> pe.getPermanentID().equalsIgnoreCase(selectedEpisodeID)).findFirst();
        if (selectedEpisode.isEmpty())
            throw new RuntimeException("");
        this.title = selectedEpisode.get().getTitle() + " - " + selectedPodcast.getTitle();

        //add(new Span(this.title), new PodlovePlayer(selectedEpisode.get()), new Span("Test"));

        add(new AudioPlayer(selectedEpisode.get()));

    }

    @Override
    public String getPageTitle() {
        return this.title;
    }
}
