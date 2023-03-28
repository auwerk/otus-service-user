package org.auwerk.otus.arch.userservice.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;

import org.auwerk.otus.arch.userservice.api.dto.UserSignUpRequestDto;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@QuarkusTest
@TestHTTPEndpoint(UserSignUpResource.class)
public class UserSignUpResourceTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @InjectMock
    UserService userService;

    @Test
    void signUp_success() {
        // given
        final var userId = 1L;
        final var request = new UserSignUpRequestDto();
        request.setUserName(USERNAME);
        request.setPassword(PASSWORD);

        // when
        Mockito.when(userService.createUser(any(UserProfile.class), anyString()))
                .thenReturn(Uni.createFrom().item(userId));

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .header("Location", Matchers.endsWith("/profile/" + userId));

        Mockito.verify(userService, times(1))
                .createUser(argThat(profile -> USERNAME.equals(profile.getUserName())), eq(PASSWORD));
    }
}
