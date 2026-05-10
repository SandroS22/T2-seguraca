import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.List;


public class BlockchainListingTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Listagem de Blockchain (Atividade 4.2.1) ---");

            
            File chainDir = new File("MiniBlockchain/data/blockchain");
            if (chainDir.exists()) {
                for (File file : chainDir.listFiles()) file.delete();
            }
            File userDir = new File("MiniBlockchain/data/users");
            if (userDir.exists()) {
                for (File file : userDir.listFiles()) file.delete();
            }

            
            System.out.println("\n[SESSAO] Alice entra no sistema...");
            ServerResponse aliceReg = MiniBlockchainServer.register("alice", "alice-pass-123");
            String aliceTotp = (String) aliceReg.getData();
            if (aliceTotp == null) throw new RuntimeException("Falha ao obter TOTP Alice: " + aliceReg.getMessage());
            
            MiniBlockchainServer.loginStep1("alice", "alice-pass-123");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(aliceTotp), "HmacSHA256"), TotpService.getCurrentTimeStep()));
            
            MiniBlockchainServer.addBlock("Bloco 0 de Alice");
            MiniBlockchainServer.addBlock("Bloco 1 de Alice");
            MiniBlockchainServer.logout();

            
            System.out.println("\n[SESSAO] Bob entra no sistema...");
            ServerResponse bobReg = MiniBlockchainServer.register("bob", "bob-pass-456");
            String bobTotp = (String) bobReg.getData();
            if (bobTotp == null) throw new RuntimeException("Falha ao obter TOTP Bob: " + bobReg.getMessage());

            MiniBlockchainServer.loginStep1("bob", "bob-pass-456");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(bobTotp), "HmacSHA256"), TotpService.getCurrentTimeStep()));
            
            MiniBlockchainServer.addBlock("Bloco 2 de Bob");
            MiniBlockchainServer.logout();

            
            System.out.println("\n[SESSAO] Alice retorna para visualizar a rede...");
            MiniBlockchainServer.loginStep1("alice", "alice-pass-123");
            MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(aliceTotp), "HmacSHA256"), TotpService.getCurrentTimeStep()));

            ServerResponse listRes = MiniBlockchainServer.getBlockchain();
            List<Block> history = (List<Block>) listRes.getData();

            System.out.println("Total de blocos recuperados: " + history.size());

            
            boolean allOk = true;
            for (int i = 0; i < history.size(); i++) {
                Block b = history.get(i);
                System.out.println("Bloco #" + b.getIndex() + " | Owner: " + b.getOwner() + " | Hash: " + b.getHash().substring(0, 10) + "...");
                
                if (Integer.parseInt(b.getIndex()) != i) {
                    System.out.println("[ERRO] Ordem incorreta no bloco " + i);
                    allOk = false;
                }
                
                if (b.getDataRaw() != null) {
                    System.out.println("[ERRO] Vazamento de dados em texto claro (dataRaw)!");
                    allOk = false;
                }
            }

            if (allOk && history.size() == 3) {
                System.out.println("\n[SUCESSO] Listagem completa, ordenada e segura validada.");
            }

            System.out.println("\n--- Fim do Teste de Listagem ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
