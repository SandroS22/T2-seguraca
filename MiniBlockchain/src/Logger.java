import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utilitário centralizado de Log para o MiniBlockchain.
 * Registra eventos operacionais e de segurança.
 */
public class Logger {

    private static final String LOG_FILE = "MiniBlockchain/data/system.log";

    public enum Level {
        INFO, WARN, ERROR, SECURITY
    }

    /**
     * Registra uma mensagem no log do sistema.
     */
    public static void log(Level level, String message) {
        try {
            String timestamp = BlockchainUtils.formatTimestamp(String.valueOf(System.currentTimeMillis()));
            
            // Tenta obter o usuário da sessão, se disponível
            String user = "ANONYMOUS";
            if (SessionContext.isLoggedIn()) {
                user = SessionContext.getCurrentUser().getUsername();
            }

            String logLine = String.format("[%s] [%-8s] [%-12s] - %s\n", 
                                           timestamp, level.name(), user, message);

            // Grava no arquivo (Append mode)
            Path path = Paths.get(LOG_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, logLine.getBytes(), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println("ERRO CRÍTICO NO LOGGER: " + e.getMessage());
        }
    }

    // Atalhos de conveniência
    public static void info(String msg) { log(Level.INFO, msg); }
    public static void warn(String msg) { log(Level.WARN, msg); }
    public static void error(String msg) { log(Level.ERROR, msg); }
    public static void security(String msg) { log(Level.SECURITY, msg); }
}
