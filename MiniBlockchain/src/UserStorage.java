import java.util.HashMap;
import java.util.Map;

/**
 * Representa o usuário como ele é gravado em disco (Cifrado).
 * Cumpre o requisito 6.vi: Apenas o salt é público.
 */
public class UserStorage {
    private String salt; // Hex (Público)
    private String iv;   // Hex (IV para o blob)
    private String blob; // Hex (JSON cifrado de User)

    public UserStorage(String salt, String iv, String blob) {
        this.salt = salt;
        this.iv = iv;
        this.blob = blob;
    }

    public String getSalt() { return salt; }
    public String getIv() { return iv; }
    public String getBlob() { return blob; }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("salt", salt);
        map.put("iv", iv);
        map.put("blob", blob);
        return map;
    }

    public static UserStorage fromMap(Map<String, String> map) {
        return new UserStorage(map.get("salt"), map.get("iv"), map.get("blob"));
    }
}
