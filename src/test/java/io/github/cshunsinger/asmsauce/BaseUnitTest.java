package io.github.cshunsinger.asmsauce;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.MockitoAnnotations.openMocks;

@SuppressWarnings("JUnit5Platform")
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class BaseUnitTest {
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void beforeEach() {
        this.mocksCloseable = openMocks(this);
    }

    @AfterEach
    @SneakyThrows
    void afterEach() {
        mocksCloseable.close();
    }
}