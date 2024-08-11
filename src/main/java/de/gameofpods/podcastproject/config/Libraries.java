package de.gameofpods.podcastproject.config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Libraries {

    private final static Logger LOGGER = LoggerFactory.getLogger(Libraries.class);

    private String groupId = null, artifactId = null, version = null, license = null, url = null;

    //groupId=com.vaadin, artifactId=vaadin, version=24.2.0, type=jar
    private Libraries(String data) {
        for (String s : data.split(",")) {
            s = s.trim();
            try {
                var tmp = s.split("=");
                if (tmp[0].equalsIgnoreCase("groupId")) {
                    groupId = tmp[1];
                } else if (tmp[0].equalsIgnoreCase("artifactId")) {
                    artifactId = tmp[1];
                } else if (tmp[0].equalsIgnoreCase("version")) {
                    version = tmp[1];
                }
            } catch (Exception ignored) {
            }
        }
        if (this.groupId == null || this.artifactId == null) {
            throw new RuntimeException("GroupId and ArtifactId cannot be null");
        }

        try {
            var tmpUrl = "https://mvnrepository.com/artifact/" + this.getGroupId() + "/" + this.getArtifactId();

            URL url = new URI(tmpUrl).toURL();
            URLConnection hc = url.openConnection();
            hc.setRequestProperty("User-Agent", "'Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148'");
            hc.setRequestProperty("Accept", "text/html");
            hc.setRequestProperty("Cache-Control", "no-cache");
            hc.setRequestProperty("Accept-Encoding", "deflate");
            hc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            hc.setRequestProperty("Accept-Charset", "UTF-8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            String line;
            StringBuilder source = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                source.append(line);
            }

            Document doc = Jsoup.parse(source.toString());
            this.url = tmpUrl;
            license = doc.select(new Evaluator() {
                @Override
                public boolean matches(Element root, Element element) {
                    return element.tagName().matches("span") && element.classNames().contains("lic");
                }
            }).getFirst().text();
        } catch (Exception ignored) {
            LOGGER.error("Could not get License for {} {}", this.groupId, this.artifactId);
        }
        LOGGER.info("Successfully parsed library {} {}-v{} @ {}", this.groupId, this.artifactId, this.version, this.url);
    }

    public String getURL() {
        return this.url;
    }

    public String getLicense() {
        return license;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public static List<Libraries> parseLibraries(String data) {
        ArrayList<Libraries> ret = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return ret;
        }
        String rePattern = "Dependency \\{.*?}";
        Pattern p = Pattern.compile(rePattern);
        Matcher m = p.matcher(data);
        while (m.find()) {
            ret.add(new Libraries(m.group(0).substring(12, m.group(0).length() - 1)));
        }
        return ret;
    }

}
