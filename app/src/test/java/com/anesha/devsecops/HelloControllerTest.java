package com.anesha.devsecops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    private final HelloController controller = new HelloController();

    @Test
    void healthReturnsUp() {
        assertEquals("UP", controller.health().get("status"));
    }

    @Test
    void homeReturnsMessage() {
        assertEquals("1.0.0", controller.home().get("version"));
    }
}
