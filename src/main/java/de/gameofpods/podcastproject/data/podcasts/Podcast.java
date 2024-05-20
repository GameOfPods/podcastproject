package de.gameofpods.podcastproject.data.podcasts;

import de.gameofpods.podcastproject.config.Config;
import dev.stalla.PodcastRssParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class Podcast implements Comparable<Podcast> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Podcast.class);

    private final static HashSet<String> PERMANENTIDS = new HashSet<>();

    private static final HashMap<String, Podcast> PODCASTS = new HashMap<>();

    static {

        var podConfig = Config.getConfig("podcasts");
        int idx = 0;
        for (String podPermanentID : podConfig.keySet()) {
            var pc = (Map<String, Object>) podConfig.get(podPermanentID);
            try {
                var tempPod = Podcast.createPodcast(idx, pc.get("url").toString(), podPermanentID);
                if (pc.containsKey("clients")) {
                    var clientsConfig = (Map<String, Object>) pc.get("clients");
                    for (String clientKey : clientsConfig.keySet()) {
                        // TODO: Iterate clients and find matching class
                        try {
                            tempPod.addClient(Client.getClientInstance(
                                    clientKey,
                                    (Map<String, String>) clientsConfig.get(clientKey),
                                    tempPod
                            ));
                        } catch (ClassCastException ignored) {
                        }
                    }
                }
                LOGGER.info("Found new Podcast " + tempPod);
                PODCASTS.put(tempPod.getPermanentID(), tempPod);
                idx++;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        PODCASTS.values().forEach(p -> {
            LOGGER.info("Updating Podcast " + p.getPermanentID());
            p.refreshFeed();
            LOGGER.info("Updated  Podcast " + p.getPermanentID() + " - " + p.getTitle());
        });
    }

    private final int idx;
    private final URL rssFeed;
    private final String permanentID;
    private final HashSet<Client> clients = new HashSet<>();
    private List<PodcastEpisode> episodeList = null;
    private dev.stalla.model.Podcast feed = null;

    private Podcast(int idx, URL rssFeed, String permanentID) throws MalformedURLException {
        this.idx = idx;
        permanentID = permanentID.toLowerCase();
        if (PERMANENTIDS.contains(permanentID))
            throw new RuntimeException("Already got a podcast with ID " + permanentID);
        this.permanentID = permanentID;
        this.rssFeed = rssFeed;
    }

    public static Podcast createPodcast(int idx, String podcastURL, String permanentID) throws MalformedURLException {
        return Podcast.createPodcast(idx, URI.create(podcastURL).toURL(), permanentID);
    }

    public static Podcast createPodcast(int idx, URL podcastURL, String permanentID) throws MalformedURLException {
        return new Podcast(idx, podcastURL, permanentID);
    }

    public static Collection<Podcast> getPodcasts() {
        return new ArrayList<>(PODCASTS.values());
    }

    public static Podcast getPodcastByPermanentID(String permanentID, Podcast def) {
        return PODCASTS.getOrDefault(permanentID, def);
    }

    public List<PodcastEpisode> getEpisodes() {
        if (this.episodeList == null) {
            this.refreshList();
        }
        return new ArrayList<>(episodeList);
    }

    public void refreshList() throws PodcastLoadException {
        this.episodeList = this.getFeed().getEpisodes().parallelStream().map(e -> PodcastEpisode.createEpisode(this, e)).sorted(Comparator.reverseOrder()).toList();
    }

    public void refreshFeed() throws PodcastLoadException {
        try {
            this.feed = PodcastRssParser.parse(this.rssFeed.openConnection(Proxy.NO_PROXY).getInputStream());
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPermanentID() {
        return permanentID;
    }

    private dev.stalla.model.Podcast getFeed() {
        if (this.feed == null)
            this.refreshFeed();
        return this.feed;
    }

    public String getTitle() {
        return this.getFeed().getTitle();
    }

    public String getAuthor() {
        try {
            var o = Objects.requireNonNull(this.getFeed().getItunes());
            return o.getAuthor();
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(this.getFeed().getGoogleplay());
            return o.getAuthor();
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    public String getDescription() {
        return this.getFeed().getDescription();
    }

    public String getSubtitle() {
        try {
            var o = Objects.requireNonNull(this.getFeed().getItunes());
            return o.getSubtitle();
        } catch (NullPointerException ignored) {
        }
        return "";
    }

    public Locale getLanguage() {
        return this.getFeed().getLanguage();
    }

    public String getImage() {
        try {
            var o = Objects.requireNonNull(this.getFeed().getImage());
            return o.getUrl();
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(this.getFeed().getItunes());
            try {
                var oo = Objects.requireNonNull(o.getImage());
                return oo.getHref();
            } catch (NullPointerException ignored) {
            }
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(this.getFeed().getGoogleplay());
            try {
                var oo = Objects.requireNonNull(o.getImage());
                return oo.getHref();
            } catch (NullPointerException ignored) {
            }
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    public String getRssFeed() {
        return rssFeed.getPath();
    }

    public String getRssURL() {
        return this.rssFeed.getProtocol() + "://" + this.rssFeed.getHost() + (this.rssFeed.getPort() > 0 ? ":" + this.rssFeed.getPort() : "") + this.rssFeed.getPath();
    }

    public Iterator<Client> iterateClients() {
        return clients.stream().sorted().iterator();
    }

    public void addClient(Client client) {
        if (client != null)
            this.clients.add(client);
    }

    @Override
    public String toString() {
        return "Podcast " + this.getPermanentID();
    }

    @Override
    public int compareTo(Podcast o) {
        var r = this.idx - o.idx;
        if (r == 0)
            r = this.getTitle().compareTo(o.getTitle());
        if (r == 0)
            r = this.getPermanentID().compareTo(o.getPermanentID());
        return -1 * r;
    }

    public static class PodcastLoadException extends RuntimeException {
        public PodcastLoadException(Exception baseException) {
            super("This exception is thrown since the Podcast could not be loaded", baseException);
        }
    }

}
