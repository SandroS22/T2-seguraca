import java.util.HashSet;
import java.util.Set;

/**
 * Teste de estresse para validar a unicidade estatística dos IVs gerados.
 */
public class IVUniquenessTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Unicidade de IV (Atividade 3.1.3) ---");
            
            int totalTests = 10000;
            System.out.println("Gerando " + totalTests + " IVs em sequencia...");
            
            Set<String> generatedIvs = new HashSet<>();
            int duplicates = 0;

            for (int i = 0; i < totalTests; i++) {
                byte[] iv = SecurityUtils.generateGcmIV();
                String ivHex = BlockchainUtils.toHex(iv);
                
                if (!generatedIvs.add(ivHex)) {
                    duplicates++;
                    System.out.println("[ALERTA] IV Duplicado detectado: " + ivHex);
                }
            }

            System.out.println("\n--- Resultados do Teste ---");
            System.out.println("Total Gerado: " + totalTests);
            System.out.println("Duplicatas Encontradas: " + duplicates);
            System.out.println("Taxa de Unicidade: " + (((double)(totalTests - duplicates) / totalTests) * 100) + "%");

            if (duplicates == 0) {
                System.out.println("[SUCESSO] Todos os " + totalTests + " IVs foram unicos.");
            } else {
                System.out.println("[FALHA] O gerador de IV produziu duplicatas!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
