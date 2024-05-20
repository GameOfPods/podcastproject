package de.gameofpods.podcastproject.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import de.gameofpods.podcastproject.data.podcasts.PodcastEpisode;
import org.json.JSONObject;

import java.util.UUID;

@Tag("div")
@NpmPackage(value = "shikwasa", version = Shikwasa.VERSION)
@JavaScript("https://cdn.jsdelivr.net/npm/shikwasa@2.2.1/dist/shikwasa.min.js")
@StyleSheet("https://cdn.jsdelivr.net/npm/shikwasa@2.2.1/dist/style.min.css")
public class Shikwasa extends Div {

    static final String VERSION = "2.2.1";
    private final String ID;
    private final PodcastEpisode episode;

    public Shikwasa(PodcastEpisode episode) {

        this.episode = episode;
        UUID uuid = UUID.randomUUID();
        ID = episode.getPermanentID() + "-" + uuid;
        this.setId(ID);
        this.addClassName("podcastproject-shikwasa");
        this.init();

    }

    private static JSONObject getConfig(PodcastEpisode episode) {
        var ret = new JSONObject();
        ret.put("title", episode.getTitle());
        ret.put("artist", episode.getPodcast().getTitle() + " - " + episode.getPodcast().getAuthor());
        var img = episode.getImageSafe();
        if (img != null)
            ret.put("cover", img);
        if (episode.getAudio() != null) {
            ret.put("src", episode.getAudio().getUrl());
            ret.put("duration", episode.getAudio().getLength());
        }
        // ret.put("audio", episode.getAudios());
        return ret;
    }

    private void init() {
        UI.getCurrent().getPage().executeJs(
                "const { Player } = window.Shikwasa\n" +
                        " const player = new Player({\n" +
                        "container: () => document.querySelector('#" + this.ID + "'),\n" +
                        "audio:" + Shikwasa.getConfig(this.episode) + ",\n" +
                        "download: true,\n" +
                        "})\n" +
                        "console.log(" + Shikwasa.getConfig(this.episode) + ");"
        );
    }

}
