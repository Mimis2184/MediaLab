package gr.medialab.ui;

import gr.medialab.app.AppBootstrap;
import gr.medialab.logic.MediaLabManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MediaLabFxApp extends Application {

    private AppBootstrap bootstrap;
    private boolean savedOnExit = false;

    @Override
    public void start(Stage stage) throws Exception {
        bootstrap = new AppBootstrap();
        MediaLabManager manager = bootstrap.start();
        AppContext.init(bootstrap, manager);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr/medialab/ui/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("MediaLab Documents");
        stage.setScene(scene);

        // Ensures application shutdown goes through Application.stop() (so JSON save happens).
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            event.consume();
        });

        stage.show();
    }

    @Override
    public void stop() {
        // Save exactly once on application exit.
        if (savedOnExit) return;
        savedOnExit = true;

        try {
            if (bootstrap != null) {
                bootstrap.stop();
            } else if (AppContext.bootstrap() != null) {
                AppContext.bootstrap().stop();
            }
        } catch (Exception ignored) {
            // No UI errors during exit
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}