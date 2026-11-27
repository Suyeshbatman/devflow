package com.devflow.marketplace.security;

import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.UUID;

// [RECORD] "Who is making this request?", in a simple form the service layer can use.
//   It keeps the SERVICE free of Spring Security types: the service only sees an id and an admin flag.
public record CurrentUser (
        UUID id, // [RECORD COMPONENT] from the token's "sub" claim
        boolean admin // [RECORD COMPONENT] true if the "roles" claim contains "ADMIN"
) {
    // [STATIC METHOD] A factory: build a CurrentUser from a Jwt OBJECT.
    public static CurrentUser from(Jwt jwt){
        // [LOCAL VARIABLE] [METHOD CALL] getClaimAsStringList("roles") returns a List<String>, or null if it's missing.
        List<String> roles = jwt.getClaimAsStringList("roles");
        // [LOCAL VARIABLE] "&&" = AND. Check for null FIRST, or roles.contains(...) would crash.
        boolean admin = roles != null && roles.contains("ADMIN");
        // [CONSTRUCTOR CALL] [STATIC METHOD CALL] UUID.fromString(...) turns the "sub" text into a UUID OBJECT.
        return new CurrentUser(UUID.fromString(jwt.getSubject()), admin);
    }
}
