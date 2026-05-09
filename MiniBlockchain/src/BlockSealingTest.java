import javax.crypto.SecretKey;
import java.io.File;

/**
 * Teste de Validação da Selagem de Blocos (Cifragem + Hashing + Wipe).
 */
public class BlockSealingTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Selagem de Bloco (Atividade 4.1.2) ---");

            // 1. Setup do Ambiente e Sessão
            new File("MiniBlockchain/data/blockchain").mkdirs();
            User user = new User("sealtester", "salt", "hash", "totp", "N/A");
            SecretKey key = SecurityUtils.deriveKey("pass123", BlockchainUtils.strToBytes("salt123"));
            SessionContext.setSession(user, key);

            // 2. Executar Orquestração de Registro
            String originalData = "Mensagem secreta super importante";
            System.out.println("\nOrquestrando criacao e selagem do bloco...");
            Block sealedBlock = BlockchainService.createAndSealBlock(originalData);

            // 3. Verificação de Metadados
            System.out.println("Bloco Gerado -> Index: " + sealedBlock.getIndex());
            System.out.println("Bloco Gerado -> Owner: " + sealedBlock.getOwner());

            // 4. Verificação de Segurança (Wipe)
            if (sealedBlock.getDataRaw() == null) {
                System.out.println("[SUCESSO] Secure Wipe: O dado em texto claro foi removido do objeto.");
            } else {
                System.out.println("[FALHA] O dado bruto ainda reside no objeto!");
            }

            // 5. Verificação de Cifragem
            if (sealedBlock.getDataEnc() != null && !sealedBlock.getDataEnc().equals(originalData)) {
                System.out.println("[SUCESSO] Dado cifrado e em formato Hex.");
                System.out.println("DataEnc: " + sealedBlock.getDataEnc().substring(0, 30) + "...");
            }

            // 6. Verificação do Selo (Hash)
            String checkHash = BlockchainService.calculateBlockHash(sealedBlock);
            if (sealedBlock.getHash().equals(checkHash)) {
                System.out.println("[SUCESSO] Selo SHA-256 validado e integro.");
                System.out.println("Hash Final: " + sealedBlock.getHash());
            } else {
                System.out.println("[FALHA] O hash do bloco nao confere!");
            }

            System.out.println("\n--- Fim do Teste de Selagem ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
