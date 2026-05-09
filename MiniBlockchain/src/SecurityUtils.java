import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 * Utilitários de Segurança para o projeto MiniBlockchain.
 * Consolida funcionalidades de KDF, Criptografia Autenticada e HMAC.
 */
public class SecurityUtils {

    private static final String PROVIDER = "BCFIPS";
    private static final int GCM_TAG_LENGTH = 128; // em bits
    private static final int GCM_IV_LENGTH = 12;   // em bytes (96 bits)
    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;      // em bits

    static {
        // Garante que o provedor está registrado
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleFipsProvider());
        }
    }

    /**
     * Deriva uma chave simétrica a partir de uma senha e um salt usando PBKDF2.
     */
    public static SecretKey deriveKey(String password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512", PROVIDER);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * Cifra dados usando AES-GCM.
     * Retorna o dado cifrado (que inclui a tag de autenticação ao final).
     */
    public static byte[] encryptAESGCM(SecretKey key, byte[] iv, byte[] plaintext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", PROVIDER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return cipher.doFinal(plaintext);
    }

    /**
     * Decifra dados usando AES-GCM.
     * Lança exceção se a integridade (tag) for violada.
     */
    public static byte[] decryptAESGCM(SecretKey key, byte[] iv, byte[] ciphertext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", PROVIDER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(ciphertext);
    }

    /**
     * Calcula o HMAC-SHA256 para um dado.
     */
    public static byte[] calculateHMAC(SecretKey key, byte[] data) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HMacSHA256", PROVIDER);
        hmac.init(key);
        return hmac.doFinal(data);
    }

    /**
     * Calcula o HMAC-SHA1 (Necessário para compatibilidade com Google Authenticator).
     */
    public static byte[] calculateHMACSHA1(SecretKey key, byte[] data) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HMacSHA1", PROVIDER);
        hmac.init(key);
        return hmac.doFinal(data);
    }

    /**
     * Calcula o hash SHA-256 de um dado (usado para encadeamento da blockchain).
     */
    public static byte[] calculateSHA256(byte[] data) throws GeneralSecurityException {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA256", PROVIDER);
        return digest.digest(data);
    }

    /**
     * Gera um salt ou IV aleatório.
     */
    public static byte[] generateRandomBytes(int size) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Gera um segredo aleatório para o TOTP (256 bits).
     */
    public static SecretKey generateTotpSecret() throws GeneralSecurityException {
        byte[] secretBytes = generateRandomBytes(32); // 256 bits
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    /**
     * Gera um IV padrão de 12 bytes para GCM.
     */
    public static byte[] generateGcmIV() {
        return generateRandomBytes(GCM_IV_LENGTH);
    }
}
