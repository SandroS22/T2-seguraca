/**
 * Encapsula a resposta de qualquer operação realizada no "Servidor".
 * Garante que apenas dados necessários retornem para o Cliente (CLI).
 */
public class ServerResponse {
    private final boolean success;
    private final String message;
    private final Object data;

    public ServerResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }

    public static ServerResponse ok(String message, Object data) {
        return new ServerResponse(true, message, data);
    }

    public static ServerResponse error(String message) {
        return new ServerResponse(false, message, null);
    }
}
