package com.github.ajoecker.gauge.graphql.login;

import com.github.ajoecker.gauge.graphql.Connector;
import com.github.ajoecker.gauge.graphql.Util;
import com.google.common.base.Strings;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ajoecker.gauge.graphql.Util.seperator;
import static java.nio.file.Files.readString;

/**
 * A {@link LoginHandler} that works based on a token.
 * <p>
 * The token can be either stated directly in the gauge environment via the configuration <code>graphql.token</code>
 * <p>
 * Or the token can be dynamically queried, when the configurations <code>graphql.token.query</code> (the file
 * with the query to login) and <code>graphql.token.path</code> (the jsonpath to the token in the response) are given.
 */
public final class TokenBasedLogin implements LoginHandler {
    private String loginToken;

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(loginToken)) {
            request.auth().oauth2(loginToken);
        }
    }

    @Override
    public void loginWithNoCredentials(Connector connector) {
        loginToken = Optional.ofNullable(System.getenv("graphql.token")).orElse(sendLoginQuery(connector, Function.identity()));
    }

    @Override
    public void loginWithCredentials(String user, String password, Connector connector) {
        loginToken = sendLoginQuery(connector, s -> Util.replaceVariablesInQuery(s, "user:" + user + seperator() + "password:" + password));
    }

    private String sendLoginQuery(Connector connector, Function<String, String> queryMapper) {
        try {
            Response sending = connector.sending(readQuery(queryMapper));
            return sending.then().extract().path(System.getenv("graphql.token.path"));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readQuery(Function<String, String> mapper) throws IOException, URISyntaxException {
        String queryFile = "/" + System.getenv("graphql.token.query");
        URI uri = TokenBasedLogin.class.getResource(queryFile).toURI();
        return mapper.apply(readString(Paths.get(uri)));
    }
}
