package com.mmsoft;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/20/16.
 */
public class IntergrationTest {

    @BeforeClass
    public static void configureRestAssured() {
        baseURI = "http://localhost";
        port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        reset();
    }

    @Test
    public void test403() {
        get("/users").
        then().statusCode(403);
    }
}
