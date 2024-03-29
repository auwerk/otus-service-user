package org.auwerk.otus.arch.userservice.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.auwerk.otus.arch.userservice.api.dto.UpdateUserProfileRequestDto;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.service.UserProfileService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;

@QuarkusTest
@TestHTTPEndpoint(UserProfileResource.class)
public class UserProfileResourceTest {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final KeycloakTestClient keycloakTestClient = new KeycloakTestClient();

    @InjectMock
    UserProfileService userService;

    @Test
    void getMyProfile_success() {
        // given
        final var userProfile = buildUserProfile();

        // when
        Mockito.when(userService.getMyProfile())
                .thenReturn(Uni.createFrom().item(userProfile));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .get()
                .then()
                .statusCode(200)
                .body("userName", Matchers.equalTo(userProfile.getUserName()))
                .body("email", Matchers.equalTo(userProfile.getEmail()))
                .body("firstName", Matchers.equalTo(userProfile.getFirstName()))
                .body("lastName", Matchers.equalTo(userProfile.getLastName()))
                .body("birthDate", Matchers.equalTo(dateFormatter.format(userProfile.getBirthDate())))
                .body("phoneNumber", Matchers.equalTo(userProfile.getPhoneNumber()));
    }

    @Test
    void getMyProfile_userProfileNotFound() {
        // when
        Mockito.when(userService.getMyProfile())
                .thenReturn(Uni.createFrom().failure(new UserProfileNotFoundException("user")));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .get()
                .then()
                .statusCode(404);
    }

    @Test
    void getMyProfile_serverError() {
        // when
        Mockito.when(userService.getMyProfile())
                .thenReturn(Uni.createFrom().failure(new RuntimeException()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .get()
                .then()
                .statusCode(500);
    }

    @Test
    void updateMyProfile_success() {
        // given
        final var request = buildUpdateProfileRequest();

        // when
        Mockito.when(userService.updateMyProfile(any(UserProfile.class)))
                .thenReturn(Uni.createFrom().voidItem());
        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .contentType(ContentType.JSON)
                .body(request)
                .put()
                .then()
                .statusCode(200);

        Mockito.verify(userService, Mockito.times(1))
                .updateMyProfile(argThat(p -> request.getEmail().equals(p.getEmail())
                        && request.getFirstName().equals(p.getFirstName())
                        && request.getLastName().equals(p.getLastName())
                        && request.getBirthDate().equals(p.getBirthDate())
                        && request.getPhoneNumber().equals(p.getPhoneNumber())));
    }

    @Test
    void updateMyProfile_userProfileNotFound() {
        // given
        final var userName = "user";
        final var request = buildUpdateProfileRequest();

        // when
        Mockito.when(userService.updateMyProfile(any(UserProfile.class)))
                .thenReturn(Uni.createFrom().failure(new UserProfileNotFoundException(userName)));
        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .contentType(ContentType.JSON)
                .body(request)
                .put()
                .then()
                .statusCode(404)
                .body(Matchers.is("user profile not found, userName=" + userName));
    }

    @Test
    void updateMyProfile_serverError() {
        // given
        final var request = buildUpdateProfileRequest();

        // when
        Mockito.when(userService.updateMyProfile(any(UserProfile.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException()));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .contentType(ContentType.JSON)
                .body(request)
                .put()
                .then()
                .statusCode(500);
    }

    @Test
    void deleteMyProfile_success() {
        // when
        Mockito.when(userService.deleteMyProfile())
                .thenReturn(Uni.createFrom().voidItem());

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .delete()
                .then()
                .statusCode(200);
    }

    @Test
    void deleteMyProfile_userProfileNotFound() {
        // given
        final var userName = "user";

        // when
        Mockito.when(userService.deleteMyProfile())
                .thenReturn(Uni.createFrom().failure(new UserProfileNotFoundException(userName)));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .delete()
                .then()
                .statusCode(404)
                .body(Matchers.is("user profile not found, userName=" + userName));
    }

    @Test
    void deleteMyProfile_serverError() {
        // given
        final var errorMessage = "test error";

        // when
        Mockito.when(userService.deleteMyProfile())
                .thenReturn(Uni.createFrom().failure(new RuntimeException(errorMessage)));

        // then
        RestAssured.given()
                .auth().oauth2(getAccessToken())
                .delete()
                .then()
                .statusCode(500)
                .body(Matchers.is(errorMessage));
    }

    private UserProfile buildUserProfile() {
        final var userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setUserName("user");
        userProfile.setEmail("user@user.com");
        userProfile.setFirstName("FirstName");
        userProfile.setLastName("LastName");
        userProfile.setBirthDate(LocalDate.of(1989, 2, 1));
        userProfile.setPhoneNumber(123456);

        return userProfile;
    }

    private UpdateUserProfileRequestDto buildUpdateProfileRequest() {
        final var request = new UpdateUserProfileRequestDto();
        request.setEmail("user@user.com");
        request.setFirstName("FirstName");
        request.setLastName("LastName");
        request.setBirthDate(LocalDate.of(1989, 2, 1));
        request.setPhoneNumber(123456);

        return request;
    }

    private String getAccessToken() {
        return keycloakTestClient.getAccessToken("bob");
    }
}
