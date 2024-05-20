package de.gameofpods.podcastproject.data.podcasts;

import dev.stalla.model.Episode;
import net.andreinc.aleph.AlephFormatter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;

import static net.andreinc.aleph.AlephFormatter.str;

public class PodcastEpisode implements Comparable<PodcastEpisode> {

    private final static Supplier<AlephFormatter> DURATION_FMT = () -> str("#{d.toHours}:#{d.toMinutesPart}:#{d.toSecondsPart}");

    private final Podcast podcast;
    private final String title, description;
    private final long publishDate;
    private final AudioEnclosure audio;
    private boolean explicit = false;
    private String episodeType = "full", episode = "0", image = null;
    private Duration duration = Duration.of(-1, ChronoUnit.SECONDS);
    private int season = 0;


    private PodcastEpisode(Podcast podcast, Episode entry) {
        this.podcast = podcast;
        this.episode = null;
        this.title = entry.getTitle();
        this.description = entry.getDescription();
        this.audio = new AudioEnclosure(entry.getEnclosure().getUrl(), entry.getEnclosure().getLength(), entry.getEnclosure().getType().toString());
        {
            var t = Objects.requireNonNullElse(entry.getPubDate(), LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
            this.publishDate = t.getLong(ChronoField.INSTANT_SECONDS);
        }
        try {
            var o = Objects.requireNonNull(entry.getItunes());
            explicit = o.getExplicit();
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(entry.getItunes());
            episodeType = o.getEpisodeType().getType();
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(entry.getItunes());
            season = o.getSeason();
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(entry.getItunes());
            try {
                var oo = Objects.requireNonNull(o.getImage());
                image = oo.getHref();
            } catch (NullPointerException ignored) {
            }
        } catch (NullPointerException ignored) {
        }
        try {
            var o = Objects.requireNonNull(entry.getItunes());
            try {
                var oo = Objects.requireNonNull(o.getDuration());
                duration = oo.getRawDuration();
            } catch (NullPointerException ignored) {
            }
        } catch (NullPointerException ignored) {
        }

    }

    public static PodcastEpisode createEpisode(Podcast podcast, Episode entry) {
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
        int r = (int) (this.getPublishDate() - o.getPublishDate());
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

    public long getPublishDate() {
        return publishDate;
    }

    public AudioEnclosure getAudio() {
        return this.audio;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getDurationString() {
        if (!this.getDuration().isNegative()) {
            return DURATION_FMT.get().arg("d", this.getDuration()).fmt();
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

    public int getSeason() {
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
