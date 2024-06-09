package de.gameofpods.podcastproject.views.podcast.podcasters;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

import static de.gameofpods.podcastproject.utils.SimpleUtils.getResource;

@WebServlet(urlPatterns = "/avatar", name = "DynamicAvatarServlet")
@AnonymousAllowed
public class PodcasterAvatar extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("image/jpeg");
        String name = req.getParameter("username");
        if (name != null) {
            //TODO: give users some avatar
        }
        var defaultAvatar = getResource("images/default_user.jpeg");
        try (OutputStream out = resp.getOutputStream()) {
            if (defaultAvatar == null) {
                return;
            }
            out.write(defaultAvatar, 0, defaultAvatar.length);
        }
    }
}
