package org.apache.coyote.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import nextstep.jwp.UserService;
import org.apache.coyote.controller.utils.PathFinder;
import org.apache.coyote.http11.httpmessage.common.ContentType;
import org.apache.coyote.http11.httpmessage.request.Request;
import org.apache.coyote.http11.httpmessage.request.requestbody.RequestBodyContent;
import org.apache.coyote.http11.httpmessage.response.Response;

public class RegisterController extends AbstractController {

    @Override
    protected void doPost(Request request, Response response) throws Exception {
        final RequestBodyContent userInput = RequestBodyContent.parse(request.getBody());
        UserService.save(userInput);

        response.redirect("/index.html")
                .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8");
    }

    @Override
    protected void doGet(Request request, Response response) throws Exception {
        final Path path = PathFinder.findByFileName("register.html");
        final String responseBody = new String(Files.readAllBytes(path));

        response.ok(responseBody)
                .addHeader("Content-Type", ContentType.HTML.getContentType() + ";charset=utf-8");
    }
}
