package de.gameofpods.podcastproject.data.podcasts.clients;


import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.data.podcasts.Client;

@Client.ClientConfigKey(id = "spotify")
public class Spotify extends Client {

    @Override
    public Anchor render() {
        var id = this.getValue("id", null);
        if (id == null)
            return null;
        Image badgeImage = new Image(
                new StreamResource("", () -> ClassLoader.getSystemResourceAsStream("images/badges/spotify/PNG/spotify-podcast-badge-wht-blk-660x160.png")),
                "Spotify Badge"
        );
        badgeImage.setMaxWidth(100, Unit.PERCENTAGE);
        badgeImage.setMaxHeight(100, Unit.PERCENTAGE);
        return new Anchor("https://open.spotify.com/show/" + id, badgeImage);
    }
}
