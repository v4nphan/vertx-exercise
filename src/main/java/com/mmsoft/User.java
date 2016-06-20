package com.mmsoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/19/16.
 */
public class User extends AbstractVerticle {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private int id;
    private String name;
    private List<Message> messages;

    public User(List<Message> messages) {
        this.messages = messages;
        id = COUNTER.getAndIncrement();
    }

    public User() {
        id = COUNTER.getAndIncrement();
        messages = new ArrayList<>();
    }

    public User(String name) {
        id = COUNTER.getAndIncrement();
        messages = new ArrayList<>();
        this.name = name;
    }

    public User(int id, String name) {
        this.id = id;
        messages = new ArrayList<>();
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages == null ? Collections.emptyList() : messages;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void addMessage(String title, String content) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(new Message(title, content));
    }

    @Override
    public void start() throws Exception {
        getVertx().eventBus().consumer(Constants.getMessages, message -> {
            User user = (User) decode(message, User.class);
            message.reply(Json.encodePrettily(user.getMessages()));
        });

        getVertx().eventBus().consumer(Constants.createUser, message -> {
            message.reply(Json.encodePrettily(new User(message.body().toString())));
        });

        getVertx().eventBus().consumer(Constants.addUserMessage, message -> {
            JsonObject body = (JsonObject) message.body();
            User user = Json.decodeValue(body.getString("user"), User.class);
            String title = body.getString("title");
            String content = body.getString("content");
            user.addMessage(content, title);
            message.reply(Json.encodePrettily(user));
        });
    }

    private Object decode(io.vertx.core.eventbus.Message<Object> message, Class c) {
        String str = message.body().toString();
        return Json.decodeValue(str, c);
    }
}
