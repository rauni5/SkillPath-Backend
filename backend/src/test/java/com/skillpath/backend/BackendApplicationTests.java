package com.skillpath.backend;

import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BackendApplicationTests {

	// Replaces the real FirebaseApp bean so context startup doesn't try to
	// parse real Google credentials during tests (none are available in CI).
	@MockitoBean
	private FirebaseApp firebaseApp;

	@Test
	void contextLoads() {
	}

}