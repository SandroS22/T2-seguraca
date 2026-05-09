import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

/**
 * Teste de Integração Final: Simula o fluxo completo da CLI.
 */
public class SystemWalkthroughTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- INICIANDO WALKTHROUGH DO SISTEMA (Atividade 5.1.1) ---");

            String user = "finaluser";
            String pass = "p@ssword12345";

            // 1. Cadastro
            System.out.println("\n[PASSO 1] Testando Cadastro...");
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            System.out.println("Status: " + regRes.getMessage());
            String totpSecretHex = (String) regRes.getData();
            SecretKey totpKey = new SecretKeySpec(BlockchainUtils.fromHex(totpSecretHex), "HmacSHA256");

            // 2. Login com Falha (Senha Errada)
            System.out.println("\n[PASSO 2] Testando Login com Senha Errada...");
            ServerResponse loginFail = MiniBlockchainServer.loginStep1(user, "wrong-pass");
            System.out.println("Resultado (Esperado erro): " + loginFail.getMessage());

            // 3. Login com Sucesso (MFA)
            System.out.println("\n[PASSO 3] Testando Login de Sucesso (MFA)...");
            MiniBlockchainServer.loginStep1(user, pass);
            String code = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
            ServerResponse loginOk = MiniBlockchainServer.loginStep2(code);
            System.out.println("Resultado: " + loginOk.getMessage());

            // 4. Registro de Blocos
            System.out.println("\n[PASSO 4] Adicionando blocos na blockchain...");
            MiniBlockchainServer.addBlock("Minha primeira transacao segura.");
            MiniBlockchainServer.addBlock("Dados confidenciais de teste.");

            // 5. Visualização Seletiva
            System.out.println("\n[PASSO 5] Listando Blockchain (Visualizacao do Dono)...");
            ServerResponse listRes = MiniBlockchainServer.getBlockchain();
            List<Block> chain = (List<Block>) listRes.getData();
            for (Block b : chain) {
                System.out.println("B#" + b.getIndex() + " [" + b.getOwner() + "] -> " + b.getDataRaw());
            }

            // 6. Auditoria Estrutural
            System.out.println("\n[PASSO 6] Executando Auditoria completa...");
            ServerResponse auditRes = MiniBlockchainServer.audit();
            System.out.println("Auditoria: " + auditRes.getMessage());

            // 7. Logout
            System.out.println("\n[PASSO 7] Executando Logout...");
            MiniBlockchainServer.logout();
            System.out.println("Esta autenticado? " + MiniBlockchainServer.isAuthenticated());

            System.out.println("\n--- WALKTHROUGH CONCLUIDO COM SUCESSO ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
