package com.mmsoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/19/16.
 */
public class DatabaseMock extends AbstractVerticle {

    private Map<Integer, User> users = new HashMap<>();

    @Override
    public void start() throws Exception {
        addSomeDataIn();
        getVertx().eventBus().consumer(Constants.getUsers, message -> {
            message.reply(Json.encodePrettily(users.values()));
        });
        getVertx().eventBus().consumer(Constants.getUserMessages, message -> {
            User user = users.get(Integer.valueOf(message.body().toString()));
            getVertx().eventBus().send(Constants.getMessages, Json.encodePrettily(user), messageAsyncResult -> {
                message.reply(messageAsyncResult.result().body().toString());
            });
        });
        getVertx().eventBus().consumer(Constants.addMessage, message -> {
            JsonObject body = (JsonObject) message.body();
            Integer id = Integer.valueOf(body.getString("id"));
            User user = users.get(id);
            body.remove("id");
            body.put("user", Json.encodePrettily(user));
            getVertx().eventBus().send(Constants.addUserMessage, body, messageAsyncResult -> {
                String result = messageAsyncResult.result().body().toString();
                users.put(id, Json.decodeValue(result, User.class));
                message.reply(result);
            });
        });
    }

    private void addSomeDataIn() {
        users.clear();
        for (int i = 1; i < 11; i++) {
            getVertx().eventBus().send(Constants.createUser, "User " + i, messageAsyncResult -> {
                User user = Json.decodeValue(messageAsyncResult.result().body().toString(), User.class);
                user.addMessage("Content", "Title");
                users.put(user.getId(), user);
            });
        }
    }
}
