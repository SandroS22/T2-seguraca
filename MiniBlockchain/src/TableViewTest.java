import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.List;


public class TableViewTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Visualizacao Tabular (Atividade 5.1.2) ---");

            
            cleanup();

            
            String totp = (String) MiniBlockchainServer.register("alice", "alice-pass-123").getData();
            MiniBlockchainServer.loginStep1("alice", "alice-pass-123");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totp), "HmacSHA256"), TotpService.getCurrentTimeStep()));

            
            System.out.println("\n[SETUP] Adicionando blocos para teste de layout...");
            MiniBlockchainServer.addBlock("Pequeno");
            MiniBlockchainServer.addBlock("Este eh um conteudo propositalmente longo para testar o truncamento de texto na tabela da cli");
            
            
            MiniBlockchainServer.logout();
            String totpBob = (String) MiniBlockchainServer.register("bob", "bob-pass-456").getData();
            MiniBlockchainServer.loginStep1("bob", "bob-pass-456");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totpBob), "HmacSHA256"), TotpService.getCurrentTimeStep()));
            MiniBlockchainServer.addBlock("Bloco do Bob");
            MiniBlockchainServer.logout();

            
            System.out.println("\n[TESTE] Visualizacao final como Alice:");
            MiniBlockchainServer.loginStep1("alice", "alice-pass-123");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totp), "HmacSHA256"), TotpService.getCurrentTimeStep()));

            
            
            System.out.println("\n--- SAÍDA RENDERIZADA ---");
            simulateMainListBlockchain();

            System.out.println("\n--- Fim do Teste Visual ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simulateMainListBlockchain() {
        ServerResponse res = MiniBlockchainServer.getBlockchain();
        List<Block> chain = (List<Block>) res.getData();
        
        String header = String.format("| %-3s | %-19s | %-12s | %-30s | %-12s |", 
                                      "ID", "DATA/HORA", "DONO", "CONTEUDO", "HASH (RES)");
        String divider = "+-----+---------------------+--------------+--------------------------------+--------------+";

        System.out.println(divider);
        System.out.println(header);
        System.out.println(divider);

        for (Block b : chain) {
            String date = BlockchainUtils.formatTimestamp(b.getTimestamp());
            String content = b.getDataRaw();
            if (content.length() > 30) content = content.substring(0, 27) + "...";
            String hashRes = b.getHash().substring(0, 12);
            System.out.println(String.format("| %-3s | %-19s | %-12s | %-30s | %-12s |", 
                               b.getIndex(), date, b.getOwner(), content, hashRes));
        }
        System.out.println(divider);
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
