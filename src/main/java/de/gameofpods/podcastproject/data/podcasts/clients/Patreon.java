package de.gameofpods.podcastproject.data.podcasts.clients;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.data.podcasts.Client;

@Client.ClientConfigKey(id = "patreon")
public class Patreon extends Client {
    @Override
    public Anchor render() {
        var name = this.getValue("name", null);
        if (name == null)
            return null;
        Image badgeImage = new Image(
                new StreamResource("", () -> ClassLoader.getSystemResourceAsStream("images/badges/patreon/PNG/PATREON_WORDMARK_1_BLACK_RGB.png")),
                "Patreon Badge"
        );
        badgeImage.setMaxWidth(100, Unit.PERCENTAGE);
        badgeImage.setMaxHeight(100, Unit.PERCENTAGE);
        return new Anchor("https://www.patreon.com/" + name, badgeImage);
    }
}
