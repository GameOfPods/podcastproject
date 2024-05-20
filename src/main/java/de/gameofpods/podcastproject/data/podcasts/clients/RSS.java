package de.gameofpods.podcastproject.data.podcasts.clients;


import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.data.podcasts.Client;

@Client.ClientConfigKey(id = "rss")
public class RSS extends Client {

    @Override
    public Anchor render() {
        Image badgeImage = new Image(
                new StreamResource("", () -> ClassLoader.getSystemResourceAsStream("images/badges/RSS/png/social_style_3_rss-512-1.png")),
                "RSS Badge"
        );
        badgeImage.setMaxWidth(100, Unit.PERCENTAGE);
        badgeImage.setMaxHeight(100, Unit.PERCENTAGE);
        return new Anchor(this.getPodcast().getRssURL(), badgeImage);
    }
}
