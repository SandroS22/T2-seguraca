package org.ufsc.service;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base32;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptoService {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

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

        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    public static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters parameters = new AEADParameters(new KeyParameter(key), GCM_TAG_LENGTH, iv);
        cipher.init(true, parameters);

        byte[] out = new byte[cipher.getOutputSize(data.length)];
        int len = cipher.processBytes(data, 0, data.length, out, 0);
        cipher.doFinal(out, len);
        return out;
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws Exception {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters parameters = new AEADParameters(new KeyParameter(key), GCM_TAG_LENGTH, iv);
        cipher.init(false, parameters);

        byte[] out = new byte[cipher.getOutputSize(cipherText.length)];
        int len = cipher.processBytes(cipherText, 0, cipherText.length, out, 0);
        cipher.doFinal(out, len);
        return out;
    }

    public static String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}