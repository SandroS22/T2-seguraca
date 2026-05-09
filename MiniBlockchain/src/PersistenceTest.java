import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import java.util.Map;

public class PersistenceTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Persistencia e Envelope Cifrado ---");

            // 1. Simulação de Cadastro
            String username = "alice";
            String password = "password123";
            byte[] salt = SecurityUtils.generateRandomBytes(16);
            SecretKey userKey = SecurityUtils.deriveKey(password, salt);
            
            // Dados sensíveis que devem ser protegidos
            String passwordHash = BlockchainUtils.toHex(SecurityUtils.calculateHMAC(userKey, BlockchainUtils.strToBytes("verify")));
            SecretKey totpSecret = SecurityUtils.generateTotpSecret();
            String totpSecretHex = BlockchainUtils.toHex(totpSecret.getEncoded());

            // Criar objeto User (em memória)
            User alice = new User(username, BlockchainUtils.toHex(salt), passwordHash, totpSecretHex, "N/A");

            // 2. Aplicar Estratégia de Envelope Cifrado para Salvar
            System.out.println("Cifrando dados do usuario " + username + "...");
            byte[] iv = SecurityUtils.generateGcmIV();
            byte[] userJsonBytes = BlockchainUtils.strToBytes(JsonUtils.mapToJson(alice.toMap()));
            byte[] blob = SecurityUtils.encryptAESGCM(userKey, iv, userJsonBytes);

            UserStorage storage = new UserStorage(
                BlockchainUtils.toHex(salt),
                BlockchainUtils.toHex(iv),
                BlockchainUtils.toHex(blob)
            );

            // Salvar no disco (usando o StorageManager adaptado para UserStorage)
            String storageJson = JsonUtils.mapToJson(storage.toMap());
            java.nio.file.Files.write(java.nio.file.Paths.get("MiniBlockchain/data/users/user_alice.json"), storageJson.getBytes());
            System.out.println("Arquivo user_alice.json salvo com sucesso.");

            // 3. Simulação de Carregamento e Login
            System.out.println("\nTentando carregar e decifrar com a senha correta...");
            byte[] loadedFileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("MiniBlockchain/data/users/user_alice.json"));
            UserStorage loadedStorage = UserStorage.fromMap(JsonUtils.jsonToMap(new String(loadedFileBytes)));
            
            // Derivar chave novamente usando a senha e o salt público
            byte[] loadedSalt = BlockchainUtils.fromHex(loadedStorage.getSalt());
            SecretKey derivedKey = SecurityUtils.deriveKey(password, loadedSalt);
            
            // Decifrar o blob
            byte[] decryptedBlob = SecurityUtils.decryptAESGCM(
                derivedKey, 
                BlockchainUtils.fromHex(loadedStorage.getIv()), 
                BlockchainUtils.fromHex(loadedStorage.getBlob())
            );

            User loadedUser = User.fromMap(JsonUtils.jsonToMap(BlockchainUtils.bytesToStr(decryptedBlob)));
            System.out.println("Usuario decifrado: " + loadedUser.getUsername());
            System.out.println("Password Hash recuperado: " + loadedUser.getPasswordHash());

            if (loadedUser.getPasswordHash().equals(passwordHash)) {
                System.out.println("[SUCESSO] Persistencia segura validada.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
