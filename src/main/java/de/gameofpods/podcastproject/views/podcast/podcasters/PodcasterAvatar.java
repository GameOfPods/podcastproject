package de.gameofpods.podcastproject.views.podcast.podcasters;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet(urlPatterns = "/avatar", name = "DynamicAvatarServlet")
@AnonymousAllowed
public class PodcasterAvatar extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("image/jpeg");
        String name = req.getParameter("username");
        if (name != null) {

        }
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("images/default_user.jpeg"); OutputStream out = resp.getOutputStream()) {
            if (in == null) {
                return;
            }
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
        }
    }
}
