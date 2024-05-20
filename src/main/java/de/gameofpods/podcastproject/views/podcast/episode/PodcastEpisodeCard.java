package de.gameofpods.podcastproject.views.podcast.episode;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.data.podcasts.PodcastEpisode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class PodcastEpisodeCard extends ListItem {

    private final static Logger LOGGER = LoggerFactory.getLogger(PodcastEpisodeCard.class);

    private PodcastEpisodeCard(Podcast podcast, PodcastEpisode episode) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE, Margin.MEDIUM);

        VerticalLayout mediaPart = new VerticalLayout();
        mediaPart.addClassNames(Background.TRANSPARENT, Display.FLEX, AlignItems.START, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        // div.setHeight("160px");
        mediaPart.setWidth(300, Unit.PIXELS);
        mediaPart.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        Image image = new Image();
        image.setWidth("100%");
        image.addClassName(Background.CONTRAST_5);
        //image.setSizeFull();
        image.setSrc(episode.getImage() == null || episode.getImage().isEmpty() ? podcast.getImage() : episode.getImage());
        image.setAlt(episode.getTitle() + " - Thumbnail");

        //  new Audio(episode.getAudios().stream().map(PodcastEpisode.AudioEnclosure::getUrl).toList(), episode.getImage(), "off", true)

        HorizontalLayout downloadInfos = new HorizontalLayout();
        if (!episode.getDuration().isNegative()) {
            Span durationBadge = new Span();
            durationBadge.getElement().getThemeList().add("badge");
            durationBadge.setText(episode.getDurationString());
            downloadInfos.add(durationBadge);
        }
        {
            Anchor d = new Anchor();
            d.setHref(episode.getAudio().getUrl());
            Duration d2 = Duration.of(episode.getAudio().getLength(), ChronoUnit.NANOS);
            d.add(
                    VaadinIcon.DOWNLOAD.create(),
                    new Span(episode.getAudio().getType())
            );
            // d.addClassName(Padding.LARGE);
            downloadInfos.add(d);
        }
        downloadInfos.setWidthFull();
        downloadInfos.setHeight(null);

        mediaPart.add(image, downloadInfos);


        Anchor header = new Anchor();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(episode.getTitle());
        header.setHref(getLink(episode));

        Div description = new Div();
        description.getStyle().set("text-wrap", "wrap");
        description.add(new Html("<text>" + episode.getDescription() + "</text>"));

        description.addClassName(Margin.Vertical.XSMALL);
//        description.getStyle().setOverflow(Style.Overflow.HIDDEN);
//        description.getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
//        description.getStyle().set("text-overflow", "ellipsis");

        //overflow: hidden;
        //white-space: nowrap; /* Don't forget this one */
        //text-overflow: ellipsis;

        HorizontalLayout badges = new HorizontalLayout();
        if (episode.isExplicit()) {
            Icon icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
            icon.getStyle().set("padding", "var(--lumo-space-xs)");
            Span badge = new Span(icon, new Span("18+"));
            badge.getElement().getThemeList().add("badge error");
            badges.add(badge);
        }
        if (episode.getSeason() >= 0) {
            Span badge = new Span();
            badge.getElement().getThemeList().add("badge");
            badge.setText("Season: " + episode.getSeason());
            badges.add(badge);
        }

        VerticalLayout contentLayout = new VerticalLayout(header, badges, description);
        contentLayout.setWidth(null);
        contentLayout.setHeightFull();
        Component layout;
        if (true) {
            layout = new HorizontalLayout(mediaPart, contentLayout);
            // ((HorizontalLayout)layout).setFlexGrow(0., div);
            ((HorizontalLayout) layout).setFlexShrink(0., mediaPart);
            // ((HorizontalLayout)layout).setFlexGrow(1., contentLayout);
        } else {
            layout = new VerticalLayout(mediaPart, contentLayout);
            ((VerticalLayout) layout).setFlexGrow(0., mediaPart);
            ((VerticalLayout) layout).setFlexGrow(1., contentLayout);
        }

        add(layout);

    }

    public static String getLink(PodcastEpisode episode) {
        return RouteConfiguration.forSessionScope().getUrl(
                PodcastEpisodeView.class, new RouteParameters(new RouteParam("podcastID", episode.getPodcast().getPermanentID()), new RouteParam("episodeID", episode.getPermanentID()))
        );
    }

    public static PodcastEpisodeCard createCard(PodcastEpisode episode) {
        return new PodcastEpisodeCard(episode.getPodcast(), episode);
    }
}
