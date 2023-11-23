package de.gameofpods.podcastproject.data.podcasts;

import com.rometools.rome.feed.synd.SyndEntry;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import de.gameofpods.podcastproject.podlove.PodlovePlayer;
import de.gameofpods.podcastproject.views.podcast.episode.PodcastEpisodeView;
import net.andreinc.aleph.AlephFormatter;
import org.jdom2.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static net.andreinc.aleph.AlephFormatter.str;

public class PodcastEpisode implements Comparable<PodcastEpisode> {

    private final static Supplier<AlephFormatter> DURATION_FMT = () -> str("#{d.toHours}:#{d.toMinutesPart}:#{d.toSecondsPart}");

    private final Podcast podcast;
    private final String title, description, descriptionType;
    private final Date publishDate, updateDate;
    private final List<AudioEnclosure> audios;
    private boolean explicit = false;
    private String episodeType = "full", season = "0", episode = "0", image = null;
    private long duration = -1;


    private PodcastEpisode(Podcast podcast, SyndEntry entry) {
        this.podcast = podcast;
        this.episode = null;
        this.title = entry.getTitle();
        this.description = entry.getDescription().getValue();
        this.descriptionType = entry.getDescription().getType() == null ? "text/plain" : entry.getDescription().getType();
        this.publishDate = entry.getPublishedDate();
        this.updateDate = entry.getUpdatedDate();
        this.audios = entry.getEnclosures().stream()
                .filter(en -> en.getType().startsWith("audio"))
                .map(en -> new AudioEnclosure(en.getUrl(), en.getLength(), en.getType()))
                .toList();
        for (Element element : entry.getForeignMarkup()) {
            try {
                switch (element.getQualifiedName()) {
                    case "itunes:explicit" -> explicit = element.getContent(0).getValue().equalsIgnoreCase("yes");
                    case "itunes:episodeType" -> episodeType = element.getContent(0).getValue();
                    case "itunes:season" -> season = element.getContent(0).getValue();
                    case "itunes:image" -> image = element.getAttributeValue("href", (String) null);
                    case "itunes:duration" -> duration = parseDuration(element.getContent(0).getValue());
                    case null, default -> {
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    public static PodcastEpisode createEpisode(Podcast podcast, SyndEntry entry) {
        return new PodcastEpisode(podcast, entry);
    }

    private static long parseDuration(String data) {
        try {
            return Long.parseLong(data);
        } catch (NumberFormatException e) {
            try {
                var sections = data.split(":");
                if (sections.length <= 3 && sections.length > 0) {
                    long m = 1, r = 0;
                    for (int i = sections.length - 1; i >= 0; i--) {
                        r += (m * Long.parseLong(sections[i]));
                        m *= 60;
                    }
                    return r;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    @Override
    public int compareTo(PodcastEpisode o) {
        int r = this.getPublishDate().compareTo(o.getPublishDate());
        if (r == 0)
            r = this.getTitle().compareTo(o.getTitle());
        if (r == 0)
            r = this.getDescription().compareTo(o.getDescription());
        return r;
    }

    public String getPermanentID() {
        return "ep_" + (this.getEpisode() + this.getSeason() + this.getTitle()).hashCode();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionType() {
        return descriptionType;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Date getNewestDate() {
        var d = this.getPublishDate();
        if (this.getUpdateDate() != null && d.compareTo(this.getUpdateDate()) < 0)
            d = this.getUpdateDate();
        return d;
    }

    public List<AudioEnclosure> getAudios() {
        return new ArrayList<>(audios);
    }

    public long getDuration() {
        return duration;
    }

    public String getDurationString() {
        if (this.getDuration() > 0) {
            return DURATION_FMT.get().arg("d", Duration.of(this.getDuration(), ChronoUnit.SECONDS)).fmt();
        }
        return "";
    }

    public boolean isExplicit() {
        return explicit;
    }

    public String getEpisodeType() {
        return episodeType;
    }

    public String getEpisode() {
        return episode;
    }

    public String getSeason() {
        return season;
    }

    public String getImage() {
        return image;
    }

    public String getImageSafe() {
        return Objects.requireNonNullElseGet(this.getImage(), () -> this.getPodcast().getImage());
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public JSONObject playerConfig() {
        var ret = new JSONObject();
        ret.put("version", PodlovePlayer.VERSION);

        ret.put("show", this.getPodcast().playerConfig());

        ret.put("title", this.getTitle());
        ret.put("summary", this.getDescription());
        ret.put("publicationDate", this.getPublishDate());
        ret.put("link",
                RouteConfiguration.forSessionScope().getUrl(
                        PodcastEpisodeView.class,
                        new RouteParameters(new RouteParam("podcastID", podcast.getPermanentID()), new RouteParam("episodeID", this.getPermanentID()))
                )
        );

        if (this.getDuration() > 0)
            ret.put("duration", this.getDurationString());
        if (this.getSeason() != null || this.getEpisode() != null)
            ret.put("subtitle", (this.getSeason() != null ? "Season: " + this.getSeason() : "") + " " + (this.getEpisode() != null ? "Episode: " + this.getEpisode() : ""));
        if (this.getImage() != null)
            ret.put("poster", this.getImage());

        var audio = new JSONArray();
        var downloadFiles = new JSONArray();
        for (AudioEnclosure audioEnclosure : this.getAudios()) {
            var audioElement = new JSONObject();
            audioElement.put("url", audioEnclosure.url);
            audioElement.put("size", audioEnclosure.getLength());
            audioElement.put("mimeType", audioEnclosure.getType());
            audio.put(audioElement);
            downloadFiles.put(audioElement);
        }
        ret.put("audio", audio);
        ret.put("files", downloadFiles);

        var contributors = new JSONArray();

        ret.put("contributors", contributors);

        return ret;
    }

    public static class AudioEnclosure {
        private final String url, type;
        private final long length;

        public AudioEnclosure(String url, long length, String type) {
            this.url = url;
            this.length = length;
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public long getLength() {
            return length;
        }

        public String getType() {
            return type;
        }
    }
}
