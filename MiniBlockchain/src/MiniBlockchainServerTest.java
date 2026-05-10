import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class MiniBlockchainServerTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Validacao da Fachada do Servidor ---");

            String user = "user" + System.currentTimeMillis();
            String pass = "secure-password-456";

            
            System.out.println("\n[CLIENTE] Solicitando registro para: " + user);
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            
            if (regRes.isSuccess()) {
                System.out.println("[CLIENTE] Registro OK. Mensagem: " + regRes.getMessage());
                String totpSecretHex = (String) regRes.getData();
                System.out.println("[CLIENTE] Segredo TOTP recebido (para configurar app): " + totpSecretHex);
                
                
                System.out.println("\n[CLIENTE] Iniciando login (Passo 1: Senha)...");
                ServerResponse login1Res = MiniBlockchainServer.loginStep1(user, pass);
                System.out.println("[CLIENTE] Resposta: " + login1Res.getMessage());

                if (login1Res.isSuccess()) {
                    
                    SecretKey totpKey = new SecretKeySpec(BlockchainUtils.fromHex(totpSecretHex), "HmacSHA256");
                    String validCode = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
                    
                    System.out.println("\n[CLIENTE] Enviando codigo TOTP gerado no app: " + validCode);
                    
                    
                    ServerResponse login2Res = MiniBlockchainServer.loginStep2(validCode);
                    System.out.println("[CLIENTE] Resposta Final: " + login2Res.getMessage());

                    if (login2Res.isSuccess()) {
                        System.out.println("\n[SUCESSO] Fluxo completo via Fachada validado.");
                        System.out.println("[SUCESSO] Cliente esta autenticado: " + MiniBlockchainServer.isAuthenticated());
                    } else {
                        System.out.println("[FALHA] O login estagio 2 falhou!");
                    }
                }
            } else {
                System.out.println("[FALHA] Registro falhou: " + regRes.getMessage());
            }

            
            System.out.println("\n[CLIENTE] Solicitando Logout...");
            MiniBlockchainServer.logout();
            System.out.println("[CLIENTE] Autenticado: " + MiniBlockchainServer.isAuthenticated());

            System.out.println("\n--- Fim do Teste de Arquitetura ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
