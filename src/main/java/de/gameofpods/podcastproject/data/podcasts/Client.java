package de.gameofpods.podcastproject.data.podcasts;

import com.vaadin.flow.component.html.Anchor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class Client implements Comparable<Client> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static final Map<String, Class<? extends Client>> clients = new HashMap<>();

    static {
        var reflections = new Reflections();
        for (Class<? extends Client> aClass : reflections.getSubTypesOf(Client.class)) {
            var clientKeyAnnotations = aClass.getDeclaredAnnotation(ClientConfigKey.class);
            if (clientKeyAnnotations != null) {
                var id = clientKeyAnnotations.id();
                if (clients.containsKey(id)) {
                    LOGGER.error("Client with id " + id + " already present");
                } else {
                    clients.put(id, aClass);
                }
            } else {
                LOGGER.error("Client " + aClass.getName() + " did have the " + ClientConfigKey.class.getName() + " annotation");
            }
        }
        LOGGER.info("Found " + clients.size() + " different client types");
    }

    private final Map<String, String> config = new HashMap<>();
    private Podcast podcast = null;

    public static Client getClientInstance(String id, Map<String, String> config, Podcast podcast) {
        if (!clients.containsKey(id))
            return null;
        try {
            var ret = clients.get(id).getDeclaredConstructor().newInstance();
            ret.setConfig(config, podcast);
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            LOGGER.error("Failed to call default constructor on " + id + "@" + clients.get(id), e);
            return null;
        }
    }

    public final String getValue(String key, String def) {
        return this.config.getOrDefault(key, def);
    }

    public final Podcast getPodcast() {
        return this.podcast;
    }

    public final void setConfig(Map<String, String> config, Podcast podcast) {
        this.config.clear();
        this.config.putAll(config);
        this.podcast = podcast;
    }

    @Override
    public int compareTo(Client o) {
        return this.getClass().getName().compareTo(o.getClass().getName());
    }

    public abstract Anchor render();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ClientConfigKey {
        String id();
    }

}
