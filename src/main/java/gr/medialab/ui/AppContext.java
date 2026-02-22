package gr.medialab.ui;

import gr.medialab.app.AppBootstrap;
import gr.medialab.domain.UserAccount;
import gr.medialab.logic.MediaLabManager;
import gr.medialab.ui.controllers.DocumentsController;
import gr.medialab.ui.controllers.MainController;
import gr.medialab.ui.controllers.SearchController;
import gr.medialab.ui.controllers.WatchlistController;

public final class AppContext {

    private static MediaLabManager manager;
    private static AppBootstrap bootstrap;
    private static UserAccount currentUser;

    private static MainController mainController;
    private static WatchlistController watchlistController;
    private static DocumentsController documentsController;
    private static SearchController searchController;

    private AppContext() {}

    public static void init(AppBootstrap b, MediaLabManager m) {
        bootstrap = b;
        manager = m;
    }

    public static MediaLabManager manager() { return manager; }
    public static AppBootstrap bootstrap() { return bootstrap; }

    public static UserAccount currentUser() { return currentUser; }
    public static void setCurrentUser(UserAccount user) { currentUser = user; }

    public static void setMainController(MainController c) { mainController = c; }
    public static MainController mainController() { return mainController; }

    public static void setWatchlistController(WatchlistController c) { watchlistController = c; }
    public static WatchlistController watchlistController() { return watchlistController; }

    public static void setDocumentsController(DocumentsController c) { documentsController = c; }
    public static DocumentsController documentsController() { return documentsController; }

    public static void setSearchController(SearchController c) { searchController = c; }
    public static SearchController searchController() { return searchController; }
}