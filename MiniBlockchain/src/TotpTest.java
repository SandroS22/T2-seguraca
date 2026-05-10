import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class TotpTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Validacao TOTP (Atividade 2.1.2) ---");

            
            SecretKey secretKey = SecurityUtils.generateTotpSecret();
            String secretHex = BlockchainUtils.toHex(secretKey.getEncoded());
            System.out.println("Segredo Gerado (Hex): " + secretHex);

            
            long currentStep = TotpService.getCurrentTimeStep();
            String code1 = TotpService.calculateTOTP(secretKey, currentStep);
            System.out.println("Codigo Atual (Janela " + currentStep + "): " + code1);

            
            boolean isValid = TotpService.validateCode(secretKey, code1);
            System.out.println("Validacao do Codigo Atual: " + (isValid ? "[OK]" : "[FALHA]"));

            
            String code2 = TotpService.calculateTOTP(secretKey, currentStep);
            if (code1.equals(code2)) {
                System.out.println("[SUCESSO] Codigo eh consistente dentro da mesma janela.");
            }

            
            String prevCode = TotpService.calculateTOTP(secretKey, currentStep - 1);
            boolean isPrevValid = TotpService.validateCode(secretKey, prevCode);
            System.out.println("Validacao de Codigo da Janela Anterior (-30s): " + (isPrevValid ? "[OK]" : "[FALHA]"));

            
            boolean isInvalidValid = TotpService.validateCode(secretKey, "000000");
            System.out.println("Validacao de Codigo Invalido (000000): " + (!isInvalidValid ? "[OK - Rejeitado]" : "[FALHA - Aceito]"));

            System.out.println("\n--- Fim do Teste TOTP ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
