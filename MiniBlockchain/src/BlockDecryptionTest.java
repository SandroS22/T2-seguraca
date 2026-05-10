import javax.crypto.SecretKey;


public class BlockDecryptionTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Decifragem e Acesso (Atividade 3.1.2) ---");

            
            User alice = new User("alice", "salt-a", "hash", "totp", "N/A");
            SecretKey keyAlice = SecurityUtils.deriveKey("senha-alice-123", BlockchainUtils.strToBytes("salt-alice-12345"));
            
            User bob = new User("bob", "salt-b", "hash", "totp", "N/A");
            SecretKey keyBob = SecurityUtils.deriveKey("senha-bob-789", BlockchainUtils.strToBytes("salt-bob-54321"));

            
            System.out.println("\n[SETUP] Alice cria um bloco confidencial...");
            SessionContext.setSession(alice, keyAlice);
            String originalMsg = "Dados privados da Alice";
            BlockchainService.BlockPayload payload = BlockchainService.encryptBlockPayload(originalMsg);
            
            Block blockAlice = new Block("1", "12345678", payload.dataEnc, payload.iv, "0000", "alice", "hash-simulado");

            
            System.out.println("[Cenario A] Alice tentando ler seu bloco...");
            String resA = BlockchainService.decryptBlockPayload(blockAlice);
            System.out.println("Resultado Alice: " + resA);
            if (resA.equals(originalMsg)) {
                System.out.println("[OK] Alice decifrou com sucesso.");
            }

            
            System.out.println("\n[Cenario B] Bob tentando ler o bloco da Alice...");
            SessionContext.clear();
            SessionContext.setSession(bob, keyBob);
            String resB = BlockchainService.decryptBlockPayload(blockAlice);
            System.out.println("Resultado Bob: " + resB);
            if (resB.contains("ACESSO NEGADO")) {
                System.out.println("[OK] Acesso negado para Bob (Bloqueio Logico).");
            }

            
            System.out.println("\n[Cenario C] Alice tenta ler seu bloco, mas o dado foi adulterado no disco...");
            SessionContext.clear();
            SessionContext.setSession(alice, keyAlice);
            
            
            String corruptedData = "F" + blockAlice.getDataEnc().substring(1);
            blockAlice.setDataEnc(corruptedData);
            
            try {
                BlockchainService.decryptBlockPayload(blockAlice);
                System.out.println("[FALHA] O sistema aceitou dados adulterados!");
            } catch (Exception e) {
                System.out.println("[OK] Sistema detectou quebra de integridade: " + e.getMessage());
            }

            System.out.println("\n--- Fim do Teste de Decifragem ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
