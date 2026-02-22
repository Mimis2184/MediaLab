package gr.medialab.app;

import gr.medialab.logic.MediaLabManager;
import gr.medialab.persistence.AppState;
import gr.medialab.persistence.JsonStorage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppBootstrap {

    private final MediaLabManager manager;
    private final JsonStorage storage;

    // Εξασφαλίζει ότι το save θα γίνει ΜΟΝΟ 1 φορά, ακόμα κι αν καλεστεί stop() από 2 σημεία (JavaFX stop + shutdown hook).
    private final AtomicBoolean saved = new AtomicBoolean(false);

    public AppBootstrap() {
        Path folder = Paths.get(System.getProperty("user.dir")).resolve("medialab");
        System.out.println("Data folder: " + folder.toAbsolutePath());

        this.storage = new JsonStorage(folder);
        this.manager = new MediaLabManager();
    }

    // Startup: Load JSON -> init objects in memory
    public MediaLabManager start() {
        try {
            AppState state = storage.loadAll();
            manager.loadFrom(state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application state", e);
        }

        // Backup save on JVM termination (CTRL+C / crash / forced exit)
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "medialab-save-hook"));

        return manager;
    }

    // Shutdown: Write JSON (runs once)
    public void stop() {
        if (!saved.compareAndSet(false, true)) {
            return; // έχει ήδη γίνει save
        }

        try {
            storage.saveAll(manager.exportState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MediaLabManager getManager() {
        return manager;
    }
}