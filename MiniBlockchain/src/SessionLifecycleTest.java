import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class SessionLifecycleTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Ciclo de Vida de Sessão (Atividade 2.2.3) ---");

            String user = "sessiontester" + System.currentTimeMillis();
            String pass = "session-pass-999";

            
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            String totpSecretHex = (String) regRes.getData();
            SecretKey totpKey = new SecretKeySpec(BlockchainUtils.fromHex(totpSecretHex), "HmacSHA256");

            
            MiniBlockchainServer.loginStep1(user, pass);
            String code = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
            MiniBlockchainServer.loginStep2(code);

            System.out.println("Status Inicial: Autenticado = " + MiniBlockchainServer.isAuthenticated());
            
            
            System.out.println("\nTentando adicionar bloco COM sessao ativa...");
            ServerResponse addRes1 = MiniBlockchainServer.addBlock("Dados de Teste");
            System.out.println("Resultado: " + addRes1.getMessage());
            if (addRes1.isSuccess()) {
                System.out.println("[OK] Operacao permitida com sessao ativa.");
            }

            
            System.out.println("\nExecutando LOGOUT...");
            MiniBlockchainServer.logout();
            System.out.println("Status Apos Logout: Autenticado = " + MiniBlockchainServer.isAuthenticated());

            
            System.out.println("\nTentando adicionar bloco SEM sessao ativa...");
            ServerResponse addRes2 = MiniBlockchainServer.addBlock("Dados de Teste");
            System.out.println("Resultado: " + addRes2.getMessage());
            if (!addRes2.isSuccess()) {
                System.out.println("[OK] Operacao bloqueada corretamente apos logout.");
            }

            
            if (SessionContext.getCurrentUser() == null && SessionContext.getSessionKey() == null) {
                System.out.println("[OK] SessionContext esta totalmente limpo (Secure Wipe).");
            }

            System.out.println("\n--- Fim do Teste de Ciclo de Vida ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
