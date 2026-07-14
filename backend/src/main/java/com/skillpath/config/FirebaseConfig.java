package com.skillpath.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import java.io.*;
import java.util.Base64;
@Configuration
public class FirebaseConfig {
    @Value("${firebase.service-account}")
    private String serviceAccountBase64;
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return FirebaseApp.getInstance();
        byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
        FirebaseOptions options = FirebaseOptions.builder()
                                                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decoded))).build();
        return FirebaseApp.initializeApp(options);
    }
}
