package de.gameofpods.podcastproject.data.podcasts;

import com.vaadin.flow.router.RouteConfiguration;
import de.gameofpods.podcastproject.data.User;
import org.json.JSONObject;

public class Contributors {
    private Contributors() {
    }

    public JSONObject UserToContributor(User user) {
        var ret = new JSONObject();
        ret.put("name", user.getName());
        ret.put("avatar", RouteConfiguration.forSessionScope().getRoute("/avatar?username=" + user.getUsername()));
        return ret;
    }

}
