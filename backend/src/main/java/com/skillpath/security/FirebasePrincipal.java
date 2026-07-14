package com.skillpath.security;
import lombok.*;
@Getter @AllArgsConstructor
public class FirebasePrincipal {
    private final String uid;
    private final String email;
}
