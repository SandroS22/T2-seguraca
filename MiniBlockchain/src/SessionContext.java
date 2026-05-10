import javax.crypto.SecretKey;


public class SessionContext {
    private static User currentUser;
    private static SecretKey sessionKey;

    
    public static void setSession(User user, SecretKey key) {
        if (currentUser != null) {
            throw new RuntimeException("Sessao ja ativa. Realize logout primeiro.");
        }
        currentUser = user;
        sessionKey = key;
    }

    
    public static void clear() {
        currentUser = null;
        sessionKey = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static SecretKey getSessionKey() {
        return sessionKey;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
