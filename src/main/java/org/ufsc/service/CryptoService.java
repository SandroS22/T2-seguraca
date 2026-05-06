package org.ufsc.service;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base32;

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
}