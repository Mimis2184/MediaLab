package gr.medialab.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import gr.medialab.domain.Document;
import gr.medialab.domain.UserAccount;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class JsonStorage {

    private final Path folder;
    private final Gson gson;

    private final Path usersFile;
    private final Path categoriesFile;
    private final Path documentsFile;
    private final Path watchlistsFile;
    private final Path watchKnownFile;

    public JsonStorage(Path folder) {
        this.folder = folder;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();

        this.usersFile = folder.resolve("users.json");
        this.categoriesFile = folder.resolve("categories.json");
        this.documentsFile = folder.resolve("documents.json");
        this.watchlistsFile = folder.resolve("watchlists.json");
        this.watchKnownFile = folder.resolve("watchKnownVersion.json");
    }

    // Διαβάζει ΟΛΑ τα JSON και επιστρέφει AppState (startup)
    public AppState loadAll() throws IOException {
        ensureFolderAndFiles();

        AppState state = new AppState();

        Type usersType = new TypeToken<List<UserAccount>>() {}.getType();
        Type docsType = new TypeToken<List<Document>>() {}.getType();
        Type categoriesType = new TypeToken<Set<String>>() {}.getType();
        Type watchlistsType = new TypeToken<Map<String, Set<String>>>() {}.getType();
        Type watchKnownType = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();

        state.users = readJson(usersFile, usersType, new ArrayList<>());
        state.documents = readJson(documentsFile, docsType, new ArrayList<>());
        state.categories = readJson(categoriesFile, categoriesType, new HashSet<>());
        state.watchlists = readJson(watchlistsFile, watchlistsType, new HashMap<>());
        state.watchKnownVersion = readJson(watchKnownFile, watchKnownType, new HashMap<>());

        return state;
    }

    // Γράφει ΟΛΗ την κατάσταση στα JSON (μόνο πριν κλείσει η εφαρμογή)
    public void saveAll(AppState state) throws IOException {
        ensureFolderAndFiles();

        writeJson(usersFile, state.users != null ? state.users : List.of());
        writeJson(documentsFile, state.documents != null ? state.documents : List.of());
        writeJson(categoriesFile, state.categories != null ? state.categories : Set.of());
        writeJson(watchlistsFile, state.watchlists != null ? state.watchlists : Map.of());
        writeJson(watchKnownFile, state.watchKnownVersion != null ? state.watchKnownVersion : Map.of());
    }

    // -------- Helpers (αρχεία/IO) ----------
    private void ensureFolderAndFiles() throws IOException {
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        ensureFile(usersFile, "[]");
        ensureFile(categoriesFile, "[]");
        ensureFile(documentsFile, "[]");
        ensureFile(watchlistsFile, "{}");
        ensureFile(watchKnownFile, "{}");
    }

    private static void ensureFile(Path file, String defaultContent) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, defaultContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
    }

    private <T> T readJson(Path file, Type type, T defaultValue) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) return defaultValue;

        T obj = gson.fromJson(json, type);
        return obj != null ? obj : defaultValue;
    }

    private void writeJson(Path file, Object obj) throws IOException {
        String json = gson.toJson(obj);
        Files.writeString(file, json, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    // -------- LocalDateTime adapter ----------
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // ISO-8601
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
