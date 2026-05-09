import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

/**
 * Teste de Simulação de Ataque Sofisticado ("Crime Perfeito").
 * Tenta burlar o SHA-256 para testar a Camada 2 (GCM).
 */
public class DualIntegrityTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Defesa em Duas Camadas (Atividade 3.2.3) ---");

            // 1. Setup: Alice cria um bloco
            User alice = new User("alice", "salt", "hash", "totp", "N/A");
            SecretKey keyAlice = SecurityUtils.deriveKey("senha-alice", BlockchainUtils.strToBytes("salt123"));
            SessionContext.setSession(alice, keyAlice);

            System.out.println("\n[SETUP] Criando bloco original da Alice...");
            BlockchainService.BlockPayload payload = BlockchainService.encryptBlockPayload("Dados Originais");
            Block block = new Block("1", "1620567890", payload.dataEnc, payload.iv, "0000", "alice", "N/A");
            block.setHash(BlockchainService.calculateBlockHash(block));

            System.out.println("Hash Original: " + block.getHash());

            // 2. ATAQUE: Adulterar dados E recalcular o Hash (Camada 1 Bypass)
            System.out.println("\n[ATAQUE] Adulterando dado cifrado e recalculando o Hash SHA-256...");
            
            // Modifica um bit do dado cifrado
            String originalDataEnc = block.getDataEnc();
            char modifiedChar = originalDataEnc.charAt(0) == '0' ? '1' : '0';
            block.setDataEnc(modifiedChar + originalDataEnc.substring(1));
            
            // Recalcula o hash para que a Camada 1 (Estrutural) ache que está tudo bem
            block.setHash(BlockchainService.calculateBlockHash(block));
            System.out.println("Novo Hash (Falsificado): " + block.getHash());

            // 3. AUDITORIA: Verificar se o sistema detecta a fraude
            System.out.println("\n[AUDITORIA] Executando verificação de integridade total...");
            ServerResponse auditRes = BlockchainService.verifyBlockFullIntegrity(block);
            
            System.out.println("Resultado da Auditoria: " + auditRes.getMessage());

            if (!auditRes.isSuccess() && auditRes.getMessage().contains("FALHA CRIPTOGRAFICA")) {
                System.out.println("\n[SUCESSO] O sistema detectou a adulteração na Camada 2 (GCM)!");
                System.out.println("[SUCESSO] O bypass do SHA-256 foi inútil sem a chave de sessão.");
            } else {
                System.out.println("\n[FALHA] O sistema foi enganado pelo novo hash!");
            }

            System.out.println("\n--- Fim do Teste de Defesa Dual ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
