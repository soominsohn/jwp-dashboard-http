package org.apache.coyote.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.model.User;
import org.apache.catalina.Session;
import org.apache.coyote.http11.httpmessage.common.ContentType;
import org.apache.coyote.http11.httpmessage.request.Request;
import org.apache.coyote.http11.httpmessage.request.requestbody.RequestBodyContent;
import org.apache.coyote.http11.httpmessage.response.Response;

public class LoginController extends AbstractController {

    @Override
    public void service(Request request, Response response) throws Exception {
        if (request.isPostMethod()) {
            doPost(request, response);
            return;
        }
        if (request.isGetMethod()) {
            doGet(request, response);
        }
    }

    @Override
    protected void doPost(Request request, Response response) throws Exception {
        final RequestBodyContent userInput = RequestBodyContent.parse(request.getBody());
        final Optional<User> user = InMemoryUserRepository.findByAccount(userInput.get("account"));

        if (user.isPresent() && user.get().checkPassword(userInput.get("password"))) {
            log.info("존재하는 유저입니다. ::: " + user);

            if (request.hasHeader("Cookie")) {
                response.redirect("/index.html")
                        .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8");
                return;
            }
            final Session session = request.getSession(!request.hasHeader("Cookie"));
            log.info("새로운 sessionId ::: " + session.getId());
            session.setAttribute("user", user);
            response.redirect("/index.html")
                    .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8")
                    .addHeader("Set-Cookie", "JSESSIONID=" + session.getId());
            return;
        }
        log.info("존재하지 않는 유저입니다. ::: " + userInput.get("account"));
        response.redirect("/401.html")
                .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8");
    }

    @Override
    protected void doGet(Request request, Response response) throws Exception {
        final URL resource = getClass().getClassLoader().getResource("static/login.html");
        final Path path = new File(resource.getPath()).toPath();

        final String responseBody = new String(Files.readAllBytes(path));

        response.ok(responseBody)
                .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8");
    }
}