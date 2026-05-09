import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.nio.ByteBuffer;

/**
 * Serviço responsável pelo cálculo de códigos TOTP (Time-based One-Time Password).
 * Utiliza HMAC-SHA256 e janelas de 30 segundos.
 */
public class TotpService {

    private static final int TIME_STEP = 30; // 30 segundos
    private static final int CODE_DIGITS = 6;

    /**
     * Retorna o passo de tempo atual (Unix Epoch / 30).
     */
    public static long getCurrentTimeStep() {
        return System.currentTimeMillis() / 1000 / TIME_STEP;
    }

    /**
     * Calcula o código TOTP para uma chave e um passo de tempo.
     */
    public static String calculateTOTP(SecretKey secretKey, long timeStep) throws GeneralSecurityException {
        // 1. Converter o timeStep para um array de 8 bytes (Big-endian)
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

        // 2. Calcular o HMAC-SHA1 (Padrão de mercado para Apps de 2FA)
        byte[] hash = SecurityUtils.calculateHMACSHA1(secretKey, data);

        // 3. Truncamento Dinâmico (conforme RFC 4226)
        int offset = hash[hash.length - 1] & 0xf;
        int binary =
            ((hash[offset] & 0x7f) << 24) |
            ((hash[offset + 1] & 0xff) << 16) |
            ((hash[offset + 2] & 0xff) << 8) |
            (hash[offset + 3] & 0xff);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }

    /**
     * Valida um código fornecido pelo usuário.
     */
    public static boolean validateCode(SecretKey secretKey, String code) throws GeneralSecurityException {
        long currentStep = getCurrentTimeStep();
        
        DebugConfig.print("Validando TOTP para o passo: " + currentStep);
        DebugConfig.print("Segredo utilizado (Hex): " + BlockchainUtils.toHex(secretKey.getEncoded()));

        for (int i = -1; i <= 1; i++) {
            String expected = calculateTOTP(secretKey, currentStep + i);
            DebugConfig.print("Janela [" + i + "] -> Codigo Esperado: " + expected);
            if (expected.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
