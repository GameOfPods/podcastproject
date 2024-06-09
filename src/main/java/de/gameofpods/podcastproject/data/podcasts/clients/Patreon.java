package de.gameofpods.podcastproject.data.podcasts.clients;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.gameofpods.podcastproject.data.podcasts.Client;

import java.io.ByteArrayInputStream;

import static de.gameofpods.podcastproject.utils.SimpleUtils.getResource;

@Client.ClientConfigKey(id = "patreon")
public class Patreon extends Client {
    @Override
    public Anchor render() {

        var name = this.getValue("name", null);
        if (name == null)
            return null;
        var res = getResource("images/badges/patreon/png/PATREON_WORDMARK_1_BLACK_RGB.png");
        if (res == null)
            res = new byte[0];
        var resFin = res;
        Image badgeImage = new Image(
                new StreamResource("", () -> new ByteArrayInputStream(resFin)),
                "Patreon Badge"
        );
        badgeImage.setMaxWidth(100, Unit.PERCENTAGE);
        badgeImage.setMaxHeight(100, Unit.PERCENTAGE);
        return new Anchor("https://www.patreon.com/" + name, badgeImage);
    }
}
