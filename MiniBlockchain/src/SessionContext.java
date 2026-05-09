import javax.crypto.SecretKey;

/**
 * Representa o contexto de uma sessão ativa após a autenticação.
 * Mantém o usuário logado e sua chave de sessão em memória.
 */
public class SessionContext {
    private static User currentUser;
    private static SecretKey sessionKey;

    /**
     * Define o contexto da sessão (chamado após login bem-sucedido).
     */
    public static void setSession(User user, SecretKey key) {
        if (currentUser != null) {
            throw new RuntimeException("Sessao ja ativa. Realize logout primeiro.");
        }
        currentUser = user;
        sessionKey = key;
    }

    /**
     * Limpa a sessão (Logout).
     */
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
