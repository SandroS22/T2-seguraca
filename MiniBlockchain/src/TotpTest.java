import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class TotpTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Validacao TOTP (Atividade 2.1.2) ---");

            // 1. Gerar um segredo de teste
            SecretKey secretKey = SecurityUtils.generateTotpSecret();
            String secretHex = BlockchainUtils.toHex(secretKey.getEncoded());
            System.out.println("Segredo Gerado (Hex): " + secretHex);

            // 2. Calcular o código para o momento atual
            long currentStep = TotpService.getCurrentTimeStep();
            String code1 = TotpService.calculateTOTP(secretKey, currentStep);
            System.out.println("Codigo Atual (Janela " + currentStep + "): " + code1);

            // 3. Validar o código atual
            boolean isValid = TotpService.validateCode(secretKey, code1);
            System.out.println("Validacao do Codigo Atual: " + (isValid ? "[OK]" : "[FALHA]"));

            // 4. Testar consistência (mesmo código na mesma janela)
            String code2 = TotpService.calculateTOTP(secretKey, currentStep);
            if (code1.equals(code2)) {
                System.out.println("[SUCESSO] Codigo eh consistente dentro da mesma janela.");
            }

            // 5. Testar janela anterior (Simulando tolerância)
            String prevCode = TotpService.calculateTOTP(secretKey, currentStep - 1);
            boolean isPrevValid = TotpService.validateCode(secretKey, prevCode);
            System.out.println("Validacao de Codigo da Janela Anterior (-30s): " + (isPrevValid ? "[OK]" : "[FALHA]"));

            // 6. Testar código inválido
            boolean isInvalidValid = TotpService.validateCode(secretKey, "000000");
            System.out.println("Validacao de Codigo Invalido (000000): " + (!isInvalidValid ? "[OK - Rejeitado]" : "[FALHA - Aceito]"));

            System.out.println("\n--- Fim do Teste TOTP ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
