package com.importorder.view;

import com.importorder.context.ApplicationContext;
import javafx.application.Platform;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewFactoryTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // Toolkit already started
        }
    }

    @Test
    void createView_loadsAllRegisteredFxml() {
        ApplicationContext.initialize();
        ViewFactory factory = new ViewFactory(ApplicationContext.getInstance()::createController);
        for (ViewType type : ViewType.values()) {
            Parent view = factory.createView(type);
            assertNotNull(view, "View should load for " + type);
        }
    }

    @Test
    void createView_rejectsNullViewType() {
        ViewFactory factory = new ViewFactory();
        assertThrows(NullPointerException.class, () -> factory.createView(null));
    }
}
