package org.ufsc.service;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base32;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class CryptoService {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    // Gera um Salt aleatório para o PBKDF2
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Deriva a senha usando PBKDF2
    public static byte[] hashPassword(char[] password, byte[] salt) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(PKCS5S2ParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, ITERATIONS);
        return ((KeyParameter) gen.generateDerivedParameters(KEY_LENGTH)).getKey();
    }

    // Gera uma secret aleatória para o TOTP (Base32)
    public static String generateTOTPSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base32.toBase32String(bytes);
    }

    public static boolean verifyTOTP(String secret, String code) {
        long timeWindow = System.currentTimeMillis() / 1000 / 30; // Janela de 30 segundos

        // Verificamos a janela atual e a anterior para tolerância a pequenos atrasos
        return getTOTPCode(secret, timeWindow).equals(code) ||
                getTOTPCode(secret, timeWindow - 1).equals(code);
    }

    private static String getTOTPCode(String secret, long time) {
        byte[] key = Base32.decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();

        HMac hmac = new HMac(new SHA1Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);

        byte[] hash = new byte[hmac.getMacSize()];
        hmac.doFinal(hash, 0);

        // Dynamic Truncation (conforme RFC 6238)
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
}