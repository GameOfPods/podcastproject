package de.gameofpods.podcastproject.config;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    private final static Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private final static HashMap<String, Config> CONFIGS = new HashMap<>();
    private final static String ENV_KEY = "PODCAST_PROJECT_CONFIG";
    private final static String[] REQUIRED_CONFIGS = new String[]{"podcasts", "application"};

    static {
        File configPath;
        try {
            configPath = new File(Objects.requireNonNull(System.getenv(ENV_KEY)));
        } catch (Exception e) {
            throw new RuntimeException("Environment variable not found \"" + ENV_KEY + "\"", e);
        }

        if (!configPath.exists() || !configPath.isDirectory())
            throw new RuntimeException("Provided path does not exist or is no directory \"" + configPath.getAbsolutePath() + "\"");

        LOGGER.info("Config directory set to \"" + configPath.getAbsolutePath() + "\"");

        try {
            for (File configFile : Objects.requireNonNull(configPath.listFiles(pathname -> pathname.isFile() && pathname.canRead()))) {
                // var configFileKey = configFile.getName().substring(0, configFile.getName().length() - 5);
                var configFileKey = configFile.getName().substring(0, configFile.getName().lastIndexOf('.'));
                LOGGER.info("Reading config \"" + configFile.getAbsolutePath() + "\" as \"" + configFileKey + "\"");
                if (CONFIGS.containsKey(configFileKey))
                    throw new RuntimeException("Config with name \"" + configFileKey + "\" already loaded");
                try {
                    var configText = Files.readString(configFile.toPath());
                    try {
                        CONFIGS.put(configFileKey, new Config(new JSONObject(configText)));
                    } catch (JSONException e) {
                        Map<String, Object> c = (new Yaml()).load(configText);
                        CONFIGS.put(configFileKey, new Config(c));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create config from \"" + configFile.getAbsolutePath() + "\"", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to iterate over directory \"" + configPath.getAbsolutePath() + "\"", e);
        }

        LOGGER.info("Loaded " + CONFIGS.size() + " config files");
        LOGGER.debug("Loaded config files: " + CONFIGS.keySet().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
        LOGGER.info(REQUIRED_CONFIGS.length + " configs are required");
        LOGGER.debug("Required configs: " + Arrays.stream(REQUIRED_CONFIGS).map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));

        for (String requiredConfig : REQUIRED_CONFIGS) {
            if (!CONFIGS.containsKey(requiredConfig))
                throw new RuntimeException("Config \"" + requiredConfig + "\" required but was not loaded");
        }

    }

    private final Map<String, Object> data;

    private Config(JSONObject configData) {
        this(configData.toMap());
    }

    private Config(Map<String, Object> data) {
        this.data = data;
    }

    public static Config getConfig(String key) {
        return CONFIGS.get(key);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Set<String> keySet() {
        return this.data.keySet();
    }

}
