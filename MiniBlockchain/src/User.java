import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar um usuário do sistema.
 */
public class User {
    private String username;
    private String salt; // Hex
    private String passwordHash; // Hex
    private String totpSecretEnc; // Hex (cifrado)
    private String totpIV; // Hex (IV para o segredo TOTP)

    public User() {}

    public User(String username, String salt, String passwordHash, String totpSecretEnc, String totpIV) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.totpSecretEnc = totpSecretEnc;
        this.totpIV = totpIV;
    }

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getTotpSecretEnc() { return totpSecretEnc; }
    public void setTotpSecretEnc(String totpSecretEnc) { this.totpSecretEnc = totpSecretEnc; }
    public String getTotpIV() { return totpIV; }
    public void setTotpIV(String totpIV) { this.totpIV = totpIV; }

    /**
     * Converte o objeto para um Map para serialização JSON.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("salt", salt);
        map.put("passwordHash", passwordHash);
        map.put("totpSecretEnc", totpSecretEnc);
        map.put("totpIV", totpIV);
        return map;
    }

    /**
     * Cria um objeto User a partir de um Map (deserialização).
     */
    public static User fromMap(Map<String, String> map) {
        return new User(
            map.get("username"),
            map.get("salt"),
            map.get("passwordHash"),
            map.get("totpSecretEnc"),
            map.get("totpIV")
        );
    }
}
