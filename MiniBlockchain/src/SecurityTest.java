import javax.crypto.SecretKey;
import java.util.Arrays;

public class SecurityTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Iniciando Teste de Segurança Integrado ---");

            
            String password = "senha-secreta-123";
            byte[] salt = SecurityUtils.generateRandomBytes(16);
            System.out.println("Senha: " + password);
            System.out.println("Salt gerado: " + BlockchainUtils.toHex(salt));

            SecretKey key = SecurityUtils.deriveKey(password, salt);
            System.out.println("Chave derivada com sucesso (AES-256).");

            
            String originalMessage = "Mensagem confidencial da MiniBlockchain";
            byte[] plaintext = BlockchainUtils.strToBytes(originalMessage);
            byte[] iv = SecurityUtils.generateGcmIV();
            
            System.out.println("\nMensagem Original: " + originalMessage);
            System.out.println("IV gerado: " + BlockchainUtils.toHex(iv));

            byte[] ciphertext = SecurityUtils.encryptAESGCM(key, iv, plaintext);
            System.out.println("Mensagem Cifrada (Hex): " + BlockchainUtils.toHex(ciphertext));

            
            byte[] decryptedBytes = SecurityUtils.decryptAESGCM(key, iv, ciphertext);
            String decryptedMessage = BlockchainUtils.bytesToStr(decryptedBytes);
            System.out.println("Mensagem Decifrada: " + decryptedMessage);

            
            if (originalMessage.equals(decryptedMessage)) {
                System.out.println("\n[SUCESSO] Round-trip de criptografia concluido com integridade.");
            } else {
                System.out.println("\n[FALHA] A mensagem decifrada eh diferente da original.");
            }

            
            System.out.println("\nTestando detecção de adulteração...");
            ciphertext[0] ^= 1; 
            try {
                SecurityUtils.decryptAESGCM(key, iv, ciphertext);
                System.out.println("[ERRO] O GCM falhou em detectar a alteração!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] GCM detectou adulteração: " + e.getMessage());
            }

            
            byte[] hmac = SecurityUtils.calculateHMAC(key, plaintext);
            System.out.println("\nHMAC-SHA256 calculado: " + BlockchainUtils.toHex(hmac));

        } catch (Exception e) {
            System.err.println("\n[ERRO FATAL] Ocorreu uma exceção durante os testes:");
            e.printStackTrace();
        }
    }
}
