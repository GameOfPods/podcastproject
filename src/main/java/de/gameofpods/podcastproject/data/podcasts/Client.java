package de.gameofpods.podcastproject.data.podcasts;

import org.json.JSONObject;

import java.util.Objects;

public abstract class Client {

    // TODO: Implement clients. Maybe use annotators?

    private final String id;
    private final String service;

    public Client(String id, String service) {
        this.id = id;
        this.service = service;
    }

    public Client(String id) {
        this(id, null);
    }

    public String getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public JSONObject getClientConfig() {
        var ret = new JSONObject();
        ret.put("id", this.getId());
        if (this.getService() != null)
            ret.put("service", this.getService());
        return ret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getService());
    }
}
