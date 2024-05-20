package de.gameofpods.podcastproject.views.podcast;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Locale;

public class PodcastCard extends Div {

    private PodcastCard(Podcast podcast, Locale locale, boolean selected) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Avatar avatar = new Avatar();
        avatar.setName(podcast.getTitle());
        avatar.setImage(podcast.getImage());

        Span name = new Span(podcast.getTitle());

        Locale lang = podcast.getLanguage();
        Span language = new Span(lang.getDisplayName(locale));
        language.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout column = new VerticalLayout(name, language);
        column.setPadding(false);
        column.setSpacing(false);

        row.add(avatar, column);
        if (selected)
            row.add(LineAwesomeIcon.HEADPHONES_SOLID.create());
        row.getStyle().set("line-height", "var(--lumo-line-height-m)");
        add(row);
    }

    public static PodcastCard render(Podcast podcast, Locale locale, boolean selected) {
        return new PodcastCard(podcast, locale, selected);
    }

}
