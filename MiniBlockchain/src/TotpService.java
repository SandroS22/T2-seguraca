import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.nio.ByteBuffer;


public class TotpService {

    private static final int TIME_STEP = 30; 
    private static final int CODE_DIGITS = 6;

    
    public static long getCurrentTimeStep() {
        return System.currentTimeMillis() / 1000 / TIME_STEP;
    }

    
    public static String calculateTOTP(SecretKey secretKey, long timeStep) throws GeneralSecurityException {
        
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

        
        byte[] hash = SecurityUtils.calculateHMACSHA1(secretKey, data);

        
        int offset = hash[hash.length - 1] & 0xf;
        int binary =
            ((hash[offset] & 0x7f) << 24) |
            ((hash[offset + 1] & 0xff) << 16) |
            ((hash[offset + 2] & 0xff) << 8) |
            (hash[offset + 3] & 0xff);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }

    
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
