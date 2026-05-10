
public class DebugConfig {
    public static boolean isEnabled = false;

    public static void print(String message) {
        if (isEnabled) {
            System.out.println("[DEBUG] " + message);
        }
    }
}
