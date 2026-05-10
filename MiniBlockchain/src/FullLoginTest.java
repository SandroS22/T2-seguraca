import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class FullLoginTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Login Completo (MFA) ---");

            String user = "fulltester" + System.currentTimeMillis();
            String pass = "strong-pass-123";

            
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            String totpSecretHex = (String) regRes.getData();
            SecretKey totpKey = new SecretKeySpec(BlockchainUtils.fromHex(totpSecretHex), "HmacSHA256");

            
            System.out.println("\n[Cenario A] Testando login de sucesso...");
            MiniBlockchainServer.loginStep1(user, pass);
            String validCode = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
            ServerResponse resA = MiniBlockchainServer.loginStep2(validCode);
            System.out.println("Resultado: " + resA.getMessage());
            if (resA.isSuccess() && MiniBlockchainServer.isAuthenticated()) {
                System.out.println("[OK] Login completo realizado com sucesso.");
            }
            MiniBlockchainServer.logout(); 

            
            System.out.println("\n[Cenario B] Tentando enviar TOTP sem ter enviado a senha...");
            ServerResponse resB = MiniBlockchainServer.loginStep2("000000");
            System.out.println("Resultado: " + resB.getMessage());
            if (!resB.isSuccess()) {
                System.out.println("[OK] Sistema impediu o bypass do Passo 1.");
            }

            
            System.out.println("\n[Cenario C] Testando limpeza de estado em caso de erro no TOTP...");
            MiniBlockchainServer.loginStep1(user, pass);
            System.out.println("Enviando TOTP INCORRETO...");
            ServerResponse resC1 = MiniBlockchainServer.loginStep2("999999");
            System.out.println("Resposta 1: " + resC1.getMessage());
            
            System.out.println("Tentando enviar TOTP CORRETO logo em seguida (sem repetir senha)...");
            String validCodeNow = TotpService.calculateTOTP(totpKey, TotpService.getCurrentTimeStep());
            ServerResponse resC2 = MiniBlockchainServer.loginStep2(validCodeNow);
            System.out.println("Resposta 2: " + resC2.getMessage());
            
            if (!resC2.isSuccess() && resC2.getMessage().contains("Fluxo de login invalido")) {
                System.out.println("[OK] Estado pendente foi limpo. Usuario obrigado a repetir a senha.");
            }

            System.out.println("\n--- Fim do Teste de Login Completo ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
