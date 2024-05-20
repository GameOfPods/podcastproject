package de.gameofpods.podcastproject.views.podcast;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodcastInformationView extends Div {

    private final static Logger LOGGER = LoggerFactory.getLogger(PodcastInformationView.class);

    private final Image podcastCover = new Image();
    private final H2 title = new H2();
    private final Div description = new Div();
    private final FlexLayout subscribeLinks = new FlexLayout();

    public PodcastInformationView(boolean horizontal) {
        this.setSizeFull();
        this.description.getStyle().set("text-wrap", "wrap");
        this.description.setMaxWidth(100, Unit.PERCENTAGE);
        this.podcastCover.setWidth(100, Unit.PERCENTAGE);
        this.subscribeLinks.addClassName(LumoUtility.Gap.SMALL);
        if (horizontal) {
            VerticalLayout tmp = new VerticalLayout(title, podcastCover);
            HorizontalLayout layout = new HorizontalLayout(tmp, subscribeLinks, description);
            podcastCover.setWidth(300, Unit.PIXELS);
            layout.setFlexGrow(0, tmp);
            layout.setFlexGrow(1, description);
            layout.setSizeFull();
            add(layout);
        } else {
            VerticalLayout layout = new VerticalLayout(title, podcastCover, subscribeLinks, description);
            layout.setFlexGrow(0, title);
            layout.setFlexGrow(0, podcastCover);
            layout.setFlexGrow(1, description);
            layout.setSizeFull();
            add(layout);
        }
    }

    public void setPodcast(Podcast podcast) {
        if (podcast != null && podcast.getImage() != null && !podcast.getImage().isEmpty()) {
            this.podcastCover.setSrc(podcast.getImage());
            this.podcastCover.setAlt("Cover - " + podcast.getTitle());
        } else {
            InputStreamFactory defaultPodcastCover = () -> ClassLoader.getSystemResourceAsStream("images/headphones.png");
            this.podcastCover.setSrc(new StreamResource("", defaultPodcastCover));
            this.podcastCover.setAlt("default podcast cover");
        }
        if (podcast != null) {
            this.title.setText(podcast.getTitle());
        } else {
            this.title.setText("#PodcastTitle");
        }
        if (podcast != null && podcast.getDescription() != null) {
            this.description.removeAll();
            this.description.add(new Html("<text>" + podcast.getDescription() + "</text>"));
        } else {
            this.description.removeAll();
            this.description.add(new Paragraph("---"));
        }
        if (podcast != null) {
            subscribeLinks.removeAll();
            podcast.iterateClients().forEachRemaining(client -> {
                var link = client.render();
                //link.setWidth(45, Unit.PERCENTAGE);
                link.setHeight(35, Unit.PIXELS);
                subscribeLinks.add(link);
            });
        }
    }

}
