import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Teste de Validação da Auditoria Completa da Blockchain.
 */
public class FullChainAuditTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Auditoria Completa (Atividade 4.2.3) ---");

            // 1. Setup Ambiente Limpo
            cleanup();

            // 2. Criar uma chain de 5 blocos
            System.out.println("\n[SETUP] Criando uma cadeia de 5 blocos...");
            String totp = (String) MiniBlockchainServer.register("admin", "admin-pass-123").getData();
            login("admin", "admin-pass-123", totp);
            
            for (int i = 0; i < 5; i++) {
                MiniBlockchainServer.addBlock("Dados do Bloco #" + i);
            }

            // 3. Cenário A: Auditoria em Cadeia Saudável
            System.out.println("\n[Cenario A] Executando auditoria na cadeia integra...");
            ServerResponse resA = MiniBlockchainServer.audit();
            System.out.println("Resultado: " + resA.getMessage());
            if (resA.isSuccess()) {
                System.out.println("[OK] Auditoria validou a cadeia saudavel.");
            }

            // 4. Cenário B: Deleção de Bloco (Furo na Cadeia)
            System.out.println("\n[Cenario B] Deletando o Bloco #2 do disco...");
            Files.delete(Paths.get("MiniBlockchain/data/blockchain/block_00002.json"));
            
            ServerResponse resB = MiniBlockchainServer.audit();
            System.out.println("Resultado: " + resB.getMessage());
            if (!resB.isSuccess() && resB.getMessage().contains("FALHA DE CONTINUIDADE")) {
                System.out.println("[OK] Auditoria detectou a deleção do bloco.");
            }

            // 5. Cenário C: Alteração de Hash (Quebra de Selo)
            cleanup(); // Reset para novo teste
            totp = (String) MiniBlockchainServer.register("admin2", "admin-pass-123").getData();
            login("admin2", "admin-pass-123", totp);
            MiniBlockchainServer.addBlock("Bloco Original");
            
            System.out.println("\n[Cenario C] Alterando o Hash do bloco no disco (Adulteração de metadado)...");
            Path path = Paths.get("MiniBlockchain/data/blockchain/block_00000.json");
            String json = new String(Files.readAllBytes(path));
            Map<String, String> map = JsonUtils.jsonToMap(json);
            map.put("hash", "falso-hash-123"); // Modifica o selo
            Files.write(path, JsonUtils.mapToJson(map).getBytes());

            ServerResponse resC = MiniBlockchainServer.audit();
            System.out.println("Resultado: " + resC.getMessage());
            if (!resC.isSuccess() && resC.getMessage().contains("FALHA DE INTEGRIDADE")) {
                System.out.println("[OK] Auditoria detectou a quebra do selo do bloco.");
            }

            System.out.println("\n--- Fim do Teste de Auditoria ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login(String user, String pass, String totpSecret) throws Exception {
        MiniBlockchainServer.loginStep1(user, pass);
        SecretKey key = new SecretKeySpec(BlockchainUtils.fromHex(totpSecret), "HmacSHA256");
        String code = TotpService.calculateTOTP(key, TotpService.getCurrentTimeStep());
        MiniBlockchainServer.loginStep2(code);
    }

    private static void cleanup() {
        File[] dirs = { new File("MiniBlockchain/data/blockchain"), new File("MiniBlockchain/data/users") };
        for (File dir : dirs) {
            if (dir.exists()) {
                for (File f : dir.listFiles()) f.delete();
            }
        }
    }
}
