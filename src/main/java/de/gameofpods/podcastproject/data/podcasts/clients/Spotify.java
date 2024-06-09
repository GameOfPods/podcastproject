package de.gameofpods.podcastproject.data.podcasts.clients;


import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.data.podcasts.Client;

import java.io.ByteArrayInputStream;

import static de.gameofpods.podcastproject.utils.SimpleUtils.getResource;

@Client.ClientConfigKey(id = "spotify")
public class Spotify extends Client {

    @Override
    public Anchor render() {
        var id = this.getValue("id", null);
        if (id == null)
            return null;
        var res = getResource("images/badges/spotify/png/spotify-podcast-badge-wht-blk-660x160.png");
        if (res == null)
            res = new byte[0];
        var resFin = res;
        Image badgeImage = new Image(
                new StreamResource("", () -> new ByteArrayInputStream(resFin)),
                "Spotify Badge"
        );
        badgeImage.setMaxWidth(100, Unit.PERCENTAGE);
        badgeImage.setMaxHeight(100, Unit.PERCENTAGE);
        return new Anchor("https://open.spotify.com/show/" + id, badgeImage);
    }
}
