import javax.crypto.SecretKey;
import java.util.HashSet;
import java.util.Set;

/**
 * Teste de Validação da Cifragem de Payloads de Bloco.
 */
public class BlockEncryptionTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Cifragem de Bloco (Atividade 3.1.1) ---");

            // 1. Simular uma sessão ativa
            System.out.println("Simulando login do usuario 'tester'...");
            User user = new User("tester", "salt", "hash", "totp", "N/A");
            SecretKey sessionKey = SecurityUtils.deriveKey("senha-de-teste-123", BlockchainUtils.strToBytes("salt123456789012"));
            SessionContext.setSession(user, sessionKey);

            // 2. Testar múltiplas cifragens e verificar unicidade do IV
            System.out.println("\nCifrando payloads e verificando IVs...");
            Set<String> ivs = new HashSet<>();
            
            for (int i = 1; i <= 5; i++) {
                String msg = "Mensagem da transacao #" + i;
                BlockchainService.BlockPayload payload = BlockchainService.encryptBlockPayload(msg);
                
                System.out.println("Bloco " + i + " -> IV: " + payload.iv + " | DataEnc: " + payload.dataEnc.substring(0, 20) + "...");
                
                if (payload.dataEnc.contains(msg)) {
                    System.out.println("[FALHA] Mensagem em texto claro encontrada no ciphertext!");
                }
                
                if (!ivs.add(payload.iv)) {
                    System.out.println("[FALHA] IV duplicado detectado: " + payload.iv);
                }
            }

            if (ivs.size() == 5) {
                System.out.println("\n[SUCESSO] Todos os 5 blocos possuem IVs unicos.");
                System.out.println("[SUCESSO] Confidencialidade validada (texto claro nao encontrado).");
            }

            // 3. Testar comportamento SEM sessao
            System.out.println("\nTestando tentativa de cifragem SEM sessao...");
            SessionContext.clear();
            try {
                BlockchainService.encryptBlockPayload("Deveria falhar");
                System.out.println("[FALHA] O sistema permitiu cifragem sem sessao!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Sistema bloqueou cifragem sem sessao: " + e.getMessage());
            }

            System.out.println("\n--- Fim do Teste de Cifragem ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
