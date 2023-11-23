package de.gameofpods.podcastproject.podlove;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import de.gameofpods.podcastproject.data.podcasts.Podcast;
import de.gameofpods.podcastproject.data.podcasts.PodcastEpisode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;


@Tag("div")
// @NpmPackage(value = "@podlove/web-player", version = PodlovePlayer.VERSION)
@JavaScript("https://cdn.podlove.org/web-player/5.x/embed.js")
public class PodlovePlayer extends Component {

    public static final String VERSION = "5";

    private final String ID;
    private final PodcastEpisode episode;

    public PodlovePlayer(PodcastEpisode episode) {
        this.episode = episode;
        UUID uuid = UUID.randomUUID();
        ID = this.episode.getPermanentID() + "-" + uuid;
        this.setId(ID);
        this.addClassName("podcastproject-podloveplayer");
        this.init();
    }

    private static JSONObject getConfig(Podcast podcast) {
        var ret = new JSONObject();
        ret.put("version", VERSION);
        ret.put("activeTab", "share");
        // ret.put("theme", null);

        var subscribe = new JSONObject();
        subscribe.put("feed", podcast.getRssFeed());

        var clients = new JSONArray();
        podcast.iterateClients().forEachRemaining(c -> clients.put(c.getClientConfig()));

        subscribe.put("clients", clients);
        ret.put("subscribe-button", subscribe);

        var share = new JSONObject();
        var shareChannel = new JSONArray();
        shareChannel.put("whats-app");
        shareChannel.put("twitter");
        shareChannel.put("facebook");
        shareChannel.put("linkedin");
        shareChannel.put("xing");
        shareChannel.put("pinterest");
        shareChannel.put("mail");
        shareChannel.put("link");

        share.put("channels", shareChannel);
        share.put("sharePlaytime", true);

        ret.put("share", share);

        return ret;
    }

    public void init() {
        var episodeConfig = this.episode.playerConfig();
        var config = getConfig(this.episode.getPodcast());
        UI.getCurrent().getPage().executeJs(
                "window.podlovePlayer('#" + ID + "', " + episodeConfig.toString() + ", " + config + ");"
        );
    }
}
