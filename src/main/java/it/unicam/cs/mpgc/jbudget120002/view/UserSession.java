package it.unicam.cs.mpgc.jbudget120002.view;

import it.unicam.cs.mpgc.jbudget120002.model.User;

/**
 * Manages the current user session.
 * This class follows the singleton pattern to ensure that there is only one
 * instance of the session throughout the application.
 */
public final class UserSession {

    private static UserSession instance;
    private User loggedInUser;

    private UserSession() {
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void clearSession() {
        loggedInUser = null;
    }
}