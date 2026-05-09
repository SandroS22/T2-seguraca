import javax.crypto.SecretKey;
import java.io.File;

/**
 * Teste de Validação da Coleta de Dados e Timestamping.
 */
public class DataCollectionTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Coleta e Metadados (Atividade 4.1.1) ---");

            // 1. Limpar ambiente
            File chainDir = new File("MiniBlockchain/data/blockchain");
            if (chainDir.exists()) {
                for (File file : chainDir.listFiles()) file.delete();
            }

            // 2. Simular Login
            User alice = new User("alice", "salt", "hash", "totp", "N/A");
            SecretKey sessionKey = SecurityUtils.deriveKey("senha-alice", BlockchainUtils.strToBytes("salt123"));
            SessionContext.setSession(alice, sessionKey);

            // 3. Testar Preparação do Bloco 0 (Gênese)
            System.out.println("\n[PASSO 1] Preparando Bloco Gênese...");
            Block b0 = BlockchainService.prepareNewBlock("Transação Gênese");
            
            System.out.println("B0 -> Index: " + b0.getIndex());
            System.out.println("B0 -> Owner: " + b0.getOwner());
            System.out.println("B0 -> Timestamp: " + b0.getTimestamp());
            System.out.println("B0 -> DataRaw: " + b0.getDataRaw());

            if (b0.getIndex().equals("0") && b0.getOwner().equals("alice")) {
                System.out.println("[OK] Metadados iniciais corretos.");
            }

            // 4. Testar Sequenciamento (Simulando que b0 foi salvo)
            // Para o teste, precisamos que o StorageManager encontre o bloco anterior no disco
            b0.setDataEnc("dummy"); b0.setIv("dummy"); b0.setHash("dummy-hash");
            StorageManager.saveBlock(b0);

            System.out.println("\n[PASSO 2] Preparando Bloco 1 (Sequencial)...");
            Block b1 = BlockchainService.prepareNewBlock("Segunda Transação");
            
            System.out.println("B1 -> Index: " + b1.getIndex());
            System.out.println("B1 -> HashPrev: " + b1.getHashPrev());

            if (b1.getIndex().equals("1") && b1.getHashPrev().equals("dummy-hash")) {
                System.out.println("[OK] Sequenciamento e vinculação inicial validados.");
            }

            // 5. Testar Segurança (Sem Sessão)
            System.out.println("\n[PASSO 3] Testando bloqueio sem sessao...");
            SessionContext.clear();
            try {
                BlockchainService.prepareNewBlock("Deveria falhar");
                System.out.println("[FALHA] Sistema permitiu coleta sem login!");
            } catch (Exception e) {
                System.out.println("[OK] Sistema barrou coleta: " + e.getMessage());
            }

            System.out.println("\n--- Fim do Teste de Coleta ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
