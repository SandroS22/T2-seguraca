import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.List;

/**
 * Teste de Persistência Real de Blocos.
 * Valida o ciclo completo: Criar -> Selar -> Salvar -> Carregar.
 */
public class FullBlockPersistenceTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Persistencia Real de Blocos (Atividade 4.1.3) ---");

            // 1. Limpar ambiente para teste isolado
            File chainDir = new File("MiniBlockchain/data/blockchain");
            if (chainDir.exists()) {
                for (File file : chainDir.listFiles()) file.delete();
            }

            // 2. Setup de Usuário e Login (Necessário para addBlock)
            String username = "persistuser";
            String password = "strong-password-123";
            
            System.out.println("\n[SESSAO] Cadastrando e Logando...");
            String totpSecretHex = (String) MiniBlockchainServer.register(username, password).getData();
            MiniBlockchainServer.loginStep1(username, password);
            
            SecretKey totpKey = new SecretKeySpec(BlockchainUtils.fromHex(totpSecretHex), "HmacSHA256");
            String code = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
            MiniBlockchainServer.loginStep2(code);

            // 3. Adicionar 3 Blocos Reais via Fachada
            System.out.println("\n[REGISTRO] Adicionando 3 blocos...");
            MiniBlockchainServer.addBlock("Conteudo do Bloco 0 (Genesis Simulada)");
            MiniBlockchainServer.addBlock("Dados da Transacao #1");
            MiniBlockchainServer.addBlock("Dados da Transacao #2");

            // 4. Verificar arquivos no disco
            String[] files = chainDir.list();
            System.out.println("Arquivos na pasta blockchain: " + files.length);
            for (String f : files) System.out.println(" -> " + f);

            if (files.length == 3) {
                System.out.println("[OK] Todos os arquivos foram criados fisicamente.");
            }

            // 5. Simular reinício do sistema (limpar memória)
            System.out.println("\n[REINICIO] Limpando sessao e recarregando do disco...");
            MiniBlockchainServer.logout();
            
            List<Block> loadedChain = StorageManager.loadAllBlocks();
            System.out.println("Total de blocos carregados: " + loadedChain.size());

            // 6. Validar Integridade da Cadeia Carregada
            boolean allOk = true;
            for (int i = 0; i < loadedChain.size(); i++) {
                Block b = loadedChain.get(i);
                System.out.println("Bloco " + b.getIndex() + " | Hash: " + b.getHash().substring(0, 10) + "...");
                
                // Verificar sequencia de indice
                if (Integer.parseInt(b.getIndex()) != i) {
                    System.out.println("[ERRO] Indice fora de sequencia no bloco " + i);
                    allOk = false;
                }

                // Verificar hashPrev (exceto no primeiro)
                if (i > 0) {
                    String prevHash = loadedChain.get(i-1).getHash();
                    if (!b.getHashPrev().equals(prevHash)) {
                        System.out.println("[ERRO] Vinculo quebrado no bloco " + i);
                        allOk = false;
                    }
                }
            }

            if (allOk) {
                System.out.println("\n[SUCESSO] Blockchain persistida e recuperada com integridade total.");
            }

            System.out.println("\n--- Fim do Teste de Persistencia ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
