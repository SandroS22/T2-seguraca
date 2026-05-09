import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import java.io.File;

/**
 * Teste para validar a persistência segura de múltiplos usuários.
 * Garante isolamento e integridade dos dados.
 */
public class MultiUserPersistenceTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste Multi-Usuário (Atividade 2.1.3) ---");

            // 1. Limpar diretório de usuários para o teste
            File userDir = new File("MiniBlockchain/data/users");
            if (userDir.exists()) {
                for (File file : userDir.listFiles()) file.delete();
            }

            // 2. Cadastrar Usuário A e Usuário B
            System.out.println("Cadastrando 'alice'...");
            String totpAlice = AuthService.register("alice", "senha-alice");
            
            System.out.println("Cadastrando 'bob'...");
            String totpBob = AuthService.register("bob", "senha-bob");

            // 3. Verificar existência de arquivos
            File fileAlice = new File("MiniBlockchain/data/users/user_alice.json");
            File fileBob = new File("MiniBlockchain/data/users/user_bob.json");

            if (fileAlice.exists() && fileBob.exists()) {
                System.out.println("[OK] Arquivos individuais criados.");
            } else {
                throw new RuntimeException("Falha na criacao dos arquivos.");
            }

            // 4. Validar Recarga e Isolamento do Usuário A
            System.out.println("\nValidando acesso de 'alice'...");
            User aliceRecarregada = loadAndDecrypt("alice", "senha-alice");
            System.out.println("Usuario carregado: " + aliceRecarregada.getUsername());
            if (aliceRecarregada.getUsername().equals("alice")) {
                System.out.println("[OK] Dados de 'alice' estao corretos.");
            }

            // 5. Validar Recarga e Isolamento do Usuário B
            System.out.println("\nValidando acesso de 'bob'...");
            User bobRecarregado = loadAndDecrypt("bob", "senha-bob");
            System.out.println("Usuario carregado: " + bobRecarregado.getUsername());
            if (bobRecarregado.getUsername().equals("bob")) {
                System.out.println("[OK] Dados de 'bob' estao corretos.");
            }

            // 6. Testar tentativa de acesso cruzado (Bob tentando ler Alice com a senha dele)
            System.out.println("\nTestando tentativa de Bob acessar Alice com 'senha-bob'...");
            try {
                loadAndDecrypt("alice", "senha-bob");
                System.out.println("[FALHA] Bob conseguiu decifrar Alice!");
            } catch (Exception e) {
                System.out.println("[OK] Acesso negado (GCM detectou erro de integridade/chave).");
            }

            System.out.println("\n--- Fim do Teste Multi-Usuário ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static User loadAndDecrypt(String username, String password) throws Exception {
        UserStorage storage = StorageManager.loadUserStorage(username);
        byte[] salt = BlockchainUtils.fromHex(storage.getSalt());
        SecretKey key = SecurityUtils.deriveKey(password, salt);
        
        byte[] decryptedBytes = SecurityUtils.decryptAESGCM(
            key, 
            BlockchainUtils.fromHex(storage.getIv()), 
            BlockchainUtils.fromHex(storage.getBlob())
        );

        return User.fromMap(JsonUtils.jsonToMap(BlockchainUtils.bytesToStr(decryptedBytes)));
    }
}
