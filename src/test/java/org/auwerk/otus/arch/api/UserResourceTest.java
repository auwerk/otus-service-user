package org.auwerk.otus.arch.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class UserResourceTest {

    @InjectMock
    UserService userService;

    @Test
    void testRegisterUserEndpoint() {
        Mockito.when(userService.createUser(Mockito.any(UserProfile.class)))
                .thenReturn(Uni.createFrom().item(1L));

        given()
                .header("Content-Type", "application/json")
                .body("{}")
                .when()
                .post("/user")
                .then()
                .statusCode(201);
    }
}
