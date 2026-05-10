import java.security.GeneralSecurityException;


public class BlockHashTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Hash de Bloco (Atividade 3.2.1) ---");

            
            Block b1 = new Block("0", "1620567890", "aabbccddeeff", "112233445566", "0000", "admin", "N/A");

            
            String hash1 = BlockchainService.calculateBlockHash(b1);
            System.out.println("Hash Original: " + hash1);

            
            String hash1_copy = BlockchainService.calculateBlockHash(b1);
            if (hash1.equals(hash1_copy)) {
                System.out.println("[SUCESSO] Hash consistente para o mesmo conteudo.");
            } else {
                System.out.println("[FALHA] Hash inconsistente!");
            }

            
            System.out.println("\nAlterando um unico caractere no DataEnc...");
            Block b2 = new Block("0", "1620567890", "aabbccddeefF", "112233445566", "0000", "admin", "N/A"); 
            String hash2 = BlockchainService.calculateBlockHash(b2);
            
            System.out.println("Novo Hash:     " + hash2);
            
            if (!hash1.equals(hash2)) {
                System.out.println("[SUCESSO] Efeito Avalanche detectado. Mudanca minima alterou o hash.");
            } else {
                System.out.println("[FALHA] O hash permaneceu o mesmo após alteração!");
            }

            
            System.out.println("\nAlterando o Owner de 'admin' para 'user1'...");
            b1.setOwner("user1");
            String hash3 = BlockchainService.calculateBlockHash(b1);
            System.out.println("Hash User1:    " + hash3);
            if (!hash1.equals(hash3)) {
                System.out.println("[OK] Mudanca de owner alterou o hash.");
            }

            System.out.println("\n--- Fim do Teste de Hash ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
