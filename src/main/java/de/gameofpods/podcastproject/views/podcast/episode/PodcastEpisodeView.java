package de.gameofpods.podcastproject.views.podcast.episode;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.gameofpods.podcastproject.components.Shikwasa;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.i18n.LanguageManager;
import de.gameofpods.podcastproject.i18n.Translations;
import de.gameofpods.podcastproject.security.AuthenticatedUser;
import de.gameofpods.podcastproject.views.MainLayout;
import de.gameofpods.podcastproject.views.MainLayoutPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;


@Route(value = "podcast/episode/:podcastID/:episodeID", layout = MainLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class PodcastEpisodeView extends MainLayoutPage implements BeforeEnterObserver, HasDynamicTitle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodcastEpisodeView.class);
    private final AuthenticatedUser authenticatedUser;
    private String title = "Podcast Episode";

    public PodcastEpisodeView(AuthenticatedUser authenticatedUser, AccessAnnotationChecker checker) {
        super(authenticatedUser, checker);
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

        HorizontalLayout mainLayout = new HorizontalLayout();
        //mainLayout.setSizeFull();
        mainLayout.setWidth(100, Unit.PERCENTAGE);

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addClassName(LumoUtility.Padding.NONE);

        leftLayout.add(new H2(selectedEpisode.get().getTitle()));

        Image cover;
        if (selectedEpisode.get().getImageSafe() == null || selectedEpisode.get().getImageSafe().isEmpty()) {
            cover = new Image(
                    new StreamResource("", () -> ClassLoader.getSystemResourceAsStream("images/headphones.png")),
                    "Episode cover - " + selectedEpisode.get().getTitle()
            );
        } else {
            cover = new Image(
                    selectedEpisode.get().getImageSafe(),
                    "Episode cover - " + selectedEpisode.get().getTitle()
            );
        }
        cover.setWidth(100, Unit.PERCENTAGE);
        leftLayout.add(cover);

        FlexLayout badges = new FlexLayout();
        badges.addClassName(LumoUtility.Gap.SMALL);
        if (selectedPodcast.getAuthor() != null && !selectedPodcast.getAuthor().isEmpty()) {
            Span authorBadge = new Span();
            authorBadge.getElement().getThemeList().add("badge");
            authorBadge.setText(selectedPodcast.getAuthor());
            badges.add(authorBadge);
        }
        if (!selectedEpisode.get().getDuration().isNegative()) {
            Span durationBadge = new Span();
            durationBadge.getElement().getThemeList().add("badge");
            durationBadge.setText(selectedEpisode.get().getDurationString());
            badges.add(durationBadge);
        }
        if (selectedEpisode.get().getPublishDate() > 0) {
            var t = LocalDateTime.ofEpochSecond(selectedEpisode.get().getPublishDate(), 0, ZoneOffset.UTC);
            Span badge = new Span();
            badge.getElement().getThemeList().add("badge");
            badge.setText(Translations.getTranslation("#publish-date", this.authenticatedUser.get().orElse(null)) + ": " + t.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            badges.add(badge);
        }
        leftLayout.add(badges);

        var leftScroller = new Scroller(leftLayout);
        leftScroller.setSizeFull();
        leftScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        leftLayout.setWidth(400, Unit.PIXELS);
        mainLayout.add(leftLayout);

        VerticalLayout rightLayout = new VerticalLayout();

        Div description = new Div();
        description.setSizeFull();
        description.getStyle().set("text-wrap", "wrap");
        description.add(new Html("<text>" + selectedEpisode.get().getDescription() + "</text>"));
        rightLayout.add(description);

        var rightScroller = new Scroller(rightLayout);
        rightScroller.setSizeFull();
        rightScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        mainLayout.add(rightLayout);
        mainLayout.setFlexGrow(0, leftLayout);
        mainLayout.setFlexGrow(1, rightLayout);

        var player = new Shikwasa(selectedEpisode.get());
        player.setWidth(100, Unit.PERCENTAGE);
//        player.getStyle().setPosition(Style.Position.FIXED);
//        player.getStyle().setBottom("0");

        VerticalLayout bigLayout = new VerticalLayout(mainLayout, player);
        bigLayout.setSizeFull();
        bigLayout.setFlexGrow(1, mainLayout);
        bigLayout.setFlexGrow(0, player);
        bigLayout.addClassName(LumoUtility.Gap.MEDIUM);

        setContent(bigLayout);

    }

    @Override
    public String getPageTitle() {
        return this.title;
    }
}
