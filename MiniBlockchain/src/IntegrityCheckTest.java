import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


public class IntegrityCheckTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Integridade de Arquivo (Atividade 2.1.3) ---");

            String user = "charlie";
            String pass = "charlie-pass";

            
            System.out.println("Cadastrando 'charlie'...");
            AuthService.register(user, pass);

            
            Path path = Paths.get("MiniBlockchain/data/users/user_charlie.json");
            String originalJson = new String(Files.readAllBytes(path));
            Map<String, String> map = JsonUtils.jsonToMap(originalJson);
            
            
            String blob = map.get("blob");
            char modifiedChar = blob.charAt(0) == '0' ? '1' : '0';
            String corruptedBlob = modifiedChar + blob.substring(1);
            map.put("blob", corruptedBlob);

            System.out.println("Adulterando o arquivo 'user_charlie.json' no disco...");
            Files.write(path, JsonUtils.mapToJson(map).getBytes());

            
            System.out.println("Tentando autenticar 'charlie' com arquivo adulterado...");
            try {
                UserStorage storage = StorageManager.loadUserStorage(user);
                byte[] salt = BlockchainUtils.fromHex(storage.getSalt());
                javax.crypto.SecretKey key = SecurityUtils.deriveKey(pass, salt);
                
                SecurityUtils.decryptAESGCM(
                    key, 
                    BlockchainUtils.fromHex(storage.getIv()), 
                    BlockchainUtils.fromHex(storage.getBlob())
                );
                
                System.out.println("[FALHA] O sistema aceitou dados adulterados!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Sistema detectou a adulteração: " + e.getMessage());
            }

            System.out.println("\n--- Fim do Teste de Integridade ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
