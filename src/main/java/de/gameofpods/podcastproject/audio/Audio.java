package de.gameofpods.podcastproject.audio;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;

import java.util.ArrayList;
import java.util.List;

@StyleSheet("https://vjs.zencdn.net/8.6.1/video-js.css")
@JavaScript("https://vjs.zencdn.net/8.6.1/video.min.js")
public class Audio extends Html {
    public Audio(List<String> url, String poster, String preload, boolean controls) {
        super(createElement(url, poster, preload, controls));
    }

    private static String createElement(List<String> url, String poster, String preload, boolean controls) {
        if (url == null || url.isEmpty())
            return "<span></span>";
        ArrayList<String> options = new ArrayList<>();
        // options.add("class=\"video-js\"");
        if (controls)
            options.add("controls");
        if (preload != null)
            options.add("preload=\"" + preload + "\"");
        if (poster != null)
            options.add("poster=\"" + poster + "\"");


        return "<audio " + String.join(" ", options) + ">" +
                String.join(" ", url.stream().map(s -> "<source src=\"" + s + "\"/>").toList()) +
                "</audio>";
    }
}
