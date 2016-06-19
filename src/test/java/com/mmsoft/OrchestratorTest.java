package com.mmsoft;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.report.ReportOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/19/16.
 */
@RunWith(VertxUnitRunner.class)
public class OrchestratorTest {

    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port)
                );

        vertx.deployVerticle(User.class.getName(), options, context.asyncAssertSuccess());
        vertx.deployVerticle(DatabaseMock.class.getName(), options, context.asyncAssertSuccess());
        vertx.deployVerticle(Orchestrator.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetUsers(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/users")
                .putHeader("KEY", "ThisIsMyKey")
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 200);
                    response.bodyHandler(body -> {
                        final JsonArray users = new JsonArray(body.toString());
                        context.assertEquals(users.size(), 10);
                        async.complete();
                    });
                })
                .end();
    }

    @Test
    public void test403(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/users")
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 403);
                    response.bodyHandler(body -> {
                        async.complete();
                    });
                })
                .end();
    }
}
