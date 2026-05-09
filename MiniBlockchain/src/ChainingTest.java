import java.io.File;
import java.util.List;

/**
 * Teste de Validação do Encadeamento da Blockchain.
 */
public class ChainingTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Encadeamento de Blockchain (Atividade 3.2.2) ---");

            // 1. Limpar blockchain existente para teste limpo
            File chainDir = new File("MiniBlockchain/data/blockchain");
            if (chainDir.exists()) {
                for (File file : chainDir.listFiles()) file.delete();
            }

            // 2. Criar Bloco 0 (Gênese)
            System.out.println("\nCriando Bloco Gênese (0)...");
            Block b0 = new Block();
            b0.setOwner("system");
            b0.setTimestamp(String.valueOf(System.currentTimeMillis()));
            b0.setDataEnc("GENESIS_DATA");
            b0.setIv("000000000000");
            
            BlockchainService.linkNewBlock(b0); // Deve setar Index 0 e HashPrev zeros
            b0.setHash(BlockchainService.calculateBlockHash(b0));
            StorageManager.saveBlock(b0);
            
            System.out.println("B0 -> Index: " + b0.getIndex() + " | Hash: " + b0.getHash().substring(0, 10) + "...");

            // 3. Criar Bloco 1 (Vinculado ao 0)
            System.out.println("\nCriando Bloco 1...");
            Block b1 = new Block();
            b1.setOwner("user1");
            b1.setTimestamp(String.valueOf(System.currentTimeMillis()));
            b1.setDataEnc("TX1_DATA");
            b1.setIv("111111111111");
            
            BlockchainService.linkNewBlock(b1); // Deve setar Index 1 e HashPrev == B0.Hash
            b1.setHash(BlockchainService.calculateBlockHash(b1));
            StorageManager.saveBlock(b1);
            
            System.out.println("B1 -> Index: " + b1.getIndex() + " | HashPrev: " + b1.getHashPrev().substring(0, 10) + "...");
            
            if (b1.getHashPrev().equals(b0.getHash())) {
                System.out.println("[OK] Bloco 1 vinculado corretamente ao Bloco 0.");
            }

            // 4. Criar Bloco 2 (Vinculado ao 1)
            System.out.println("\nCriando Bloco 2...");
            Block b2 = new Block();
            b2.setOwner("user1");
            b2.setTimestamp(String.valueOf(System.currentTimeMillis()));
            b2.setDataEnc("TX2_DATA");
            b2.setIv("222222222222");
            
            BlockchainService.linkNewBlock(b2); 
            b2.setHash(BlockchainService.calculateBlockHash(b2));
            StorageManager.saveBlock(b2);
            
            System.out.println("B2 -> Index: " + b2.getIndex() + " | HashPrev: " + b2.getHashPrev().substring(0, 10) + "...");
            
            if (b2.getHashPrev().equals(b1.getHash())) {
                System.out.println("[OK] Bloco 2 vinculado corretamente ao Bloco 1.");
            }

            // 5. Validação de Persistência e Ordem
            System.out.println("\nValidando leitura da cadeia do disco...");
            List<Block> chain = StorageManager.loadAllBlocks();
            System.out.println("Tamanho da cadeia lida: " + chain.size());
            
            if (chain.size() == 3 && chain.get(2).getIndex().equals("2")) {
                System.out.println("[SUCESSO] Cadeia persistida e ordenada corretamente.");
            }

            System.out.println("\n--- Fim do Teste de Encadeamento ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
