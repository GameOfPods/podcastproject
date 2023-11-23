package de.gameofpods.podcastproject.components.audioplayer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.audio.Audio;
import de.gameofpods.podcastproject.data.podcasts.PodcastEpisode;

import java.util.Objects;

public class AudioPlayer extends Div {

    public AudioPlayer(PodcastEpisode podcastEpisode) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        Image img;
        try {
            img = new Image(Objects.requireNonNull(podcastEpisode.getImage()), podcastEpisode.getTitle() + " - Cover");
        } catch (NullPointerException e) {
            StreamResource res = new StreamResource("", () -> ClassLoader.getSystemResourceAsStream("images/headphones.png"));
            img = new Image(res, podcastEpisode.getTitle() + " - Cover");
        }
        img.setHeight(300, Unit.PIXELS);
        img.setWidth(null);

        layout.add(img);

        VerticalLayout content = new VerticalLayout();
        Component title = new H3(podcastEpisode.getTitle());

        content.add(title);

        String t = "";
        if (podcastEpisode.getSeason() != null)
            t += " Season " + podcastEpisode.getSeason();
        if (podcastEpisode.getEpisode() != null)
            t += " Episode " + podcastEpisode.getEpisode();
        Component subtitle = new H6(t);

        content.add(subtitle);

        Component audio = new Audio(
                podcastEpisode.getAudios().stream().map(PodcastEpisode.AudioEnclosure::getUrl).toList(),
                podcastEpisode.getImage(),
                "false",
                true
        );

        content.add(audio);

        layout.add(content);

        this.add(layout);
    }

}
