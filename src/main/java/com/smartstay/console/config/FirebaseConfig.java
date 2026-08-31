package com.smartstay.console.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Autowired
    private Environment environment;

    @Bean
    FirebaseMessaging configureFirebase() throws IOException {

        GoogleCredentials googleCredentials = null;

        if (environment.matchesProfiles("prod")) {
            googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("ss-admin.json").getInputStream());
        }
        else {
            googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("ss-admin.json").getInputStream());
        }

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        FirebaseApp app;

        try {
            app = FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            app = FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseMessaging.getInstance(app);
    }
}
