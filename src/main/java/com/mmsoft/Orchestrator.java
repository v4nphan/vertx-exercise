package com.mmsoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/19/16.
 */
public class Orchestrator extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        getVertx().deployVerticle(User.class.getName());
        getVertx().deployVerticle(DatabaseMock.class.getName());
        Router router = Router.router(vertx);

        router.route("/").handler(StaticHandler.create("assets").setIndexPage("index.html"));
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.route("/users/*").handler(httpReq -> {
            String key = httpReq.request().getHeader("KEY");
            if (key == null || key.isEmpty() || !"ThisIsMyKey".equals(key)) {
                HttpServerResponse response = httpReq.response();
                response.setStatusCode(403);
                response.putHeader("content-type", "application/json");
                response.end(new JsonObject().put("code", -403).toString());
            } else {
                httpReq.next();
            }
        });

        router.get("/users").handler(this::getAllUsers);
        router.get("/users/:id/messages").handler(this::getUserMessages);
        router.post("/users/:id/messages").handler(this::addMessage);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    private void getUserMessages(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            vertx.eventBus().send(Constants.getUserMessages, id, messageAsyncResult -> {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(messageAsyncResult.result().body().toString());
            });
        }
    }

    private void addMessage(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            routingContext.request().bodyHandler(data -> {
                JsonObject body = new JsonObject(data.toString());
                body.put("id", id);
                vertx.eventBus().send(Constants.addMessage, body, messageAsyncResult -> {
                    routingContext.response()
                            .putHeader("content-type", "text/html")
                            .end("Done");
                });
            });


        }
    }

    private void getAllUsers(RoutingContext routingContext) {
        vertx.eventBus().send(Constants.getUsers, null, messageAsyncResult -> {
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(messageAsyncResult.result().body().toString());
        });
    }
}
