import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Teste de Validação da Decifragem Seletiva e Privacidade Multiusuário.
 */
public class SelectiveDecryptionTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Decifragem Seletiva (Atividade 4.2.2) ---");

            // 1. Limpar ambiente
            cleanup();

            // 2. Setup Alice (Cria Bloco 0 e 1)
            System.out.println("\n[SESSAO] Alice registra seus blocos...");
            String aliceTotp = (String) MiniBlockchainServer.register("alice", "alice-pass-123").getData();
            login("alice", "alice-pass-123", aliceTotp);
            MiniBlockchainServer.addBlock("Conteudo Privado Alice #0");
            MiniBlockchainServer.addBlock("Conteudo Privado Alice #1");
            MiniBlockchainServer.logout();

            // 3. Setup Bob (Cria Bloco 2)
            System.out.println("\n[SESSAO] Bob registra seu bloco...");
            String bobTotp = (String) MiniBlockchainServer.register("bob", "bob-pass-456").getData();
            login("bob", "bob-pass-456", bobTotp);
            MiniBlockchainServer.addBlock("Conteudo Privado Bob #2");
            MiniBlockchainServer.logout();

            // 4. Cenário A: Alice visualiza a chain
            System.out.println("\n[Cenario A] Alice logada visualizando a chain:");
            login("alice", "alice-pass-123", aliceTotp);
            ServerResponse aliceView = MiniBlockchainServer.getBlockchain();
            printChainView((List<Block>) aliceView.getData());
            MiniBlockchainServer.logout();

            // 5. Cenário B: Bob visualiza a chain
            System.out.println("\n[Cenario B] Bob logado visualizando a chain:");
            login("bob", "bob-pass-456", bobTotp);
            ServerResponse bobView = MiniBlockchainServer.getBlockchain();
            printChainView((List<Block>) bobView.getData());
            
            // 6. Cenário C: Detecção de Adulteração na Listagem
            System.out.println("\n[Cenario C] Adulterando Bloco #2 (de Bob) e visualizando como Bob...");
            tamperBlock(2);
            ServerResponse bobViewCorrupted = MiniBlockchainServer.getBlockchain();
            printChainView((List<Block>) bobViewCorrupted.getData());
            MiniBlockchainServer.logout();

            System.out.println("\n--- Fim do Teste de Decifragem Seletiva ---");

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

    private static void printChainView(List<Block> chain) {
        for (Block b : chain) {
            System.out.println("Bloco #" + b.getIndex() + " | Owner: " + b.getOwner() + " | Conteudo: " + b.getDataRaw());
        }
    }

    private static void tamperBlock(int index) throws Exception {
        String filename = String.format("block_%05d.json", index);
        java.nio.file.Path path = java.nio.file.Paths.get("MiniBlockchain/data/blockchain", filename);
        String json = new String(java.nio.file.Files.readAllBytes(path));
        Map<String, String> map = JsonUtils.jsonToMap(json);
        String blob = map.get("dataEnc");
        char mod = blob.charAt(0) == '0' ? '1' : '0';
        map.put("dataEnc", mod + blob.substring(1));
        java.nio.file.Files.write(path, JsonUtils.mapToJson(map).getBytes());
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
