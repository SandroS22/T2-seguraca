import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;


public class AuthService {

    
    public static String register(String username, String password) throws GeneralSecurityException, IOException {
        
        if (username == null || username.length() < 3 || !username.matches("[a-zA-Z0-9]+")) {
            throw new RuntimeException("Username invalido. Deve ter pelo menos 3 caracteres alfanumericos.");
        }
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Senha invalida. Deve ter pelo menos 8 caracteres.");
        }
        if (password.equals(username)) {
            throw new RuntimeException("A senha nao pode ser igual ao username.");
        }

        if (StorageManager.userExists(username)) {
            throw new RuntimeException("Usuario ja cadastrado: " + username);
        }

        
        byte[] salt = SecurityUtils.generateRandomBytes(16);
        String saltHex = BlockchainUtils.toHex(salt);

        
        SecretKey masterKey = SecurityUtils.deriveKey(password, salt);

        
        byte[] verifier = SecurityUtils.calculateHMAC(masterKey, BlockchainUtils.strToBytes("auth-verifier"));
        String passwordHash = BlockchainUtils.toHex(verifier);

        
        SecretKey totpSecret = SecurityUtils.generateTotpSecret();
        String totpHex = BlockchainUtils.toHex(totpSecret.getEncoded());

        
        User newUser = new User(username, saltHex, passwordHash, totpHex, "N/A");

        
        byte[] iv = SecurityUtils.generateGcmIV();
        byte[] userJsonBytes = BlockchainUtils.strToBytes(JsonUtils.mapToJson(newUser.toMap()));
        byte[] blob = SecurityUtils.encryptAESGCM(masterKey, iv, userJsonBytes);

        UserStorage storage = new UserStorage(
            saltHex,
            BlockchainUtils.toHex(iv),
            BlockchainUtils.toHex(blob)
        );

        
        StorageManager.saveUserStorage(username, storage);

        Logger.info("Novo usuario registrado: " + username);

        
        return BlockchainUtils.toBase32(totpSecret.getEncoded()); 
    }

    
    public static User authenticateStep1(String username, String password) throws GeneralSecurityException, IOException {
        UserStorage storage = StorageManager.loadUserStorage(username);
        if (storage == null) {
            throw new RuntimeException("Usuario nao encontrado: " + username);
        }

        
        byte[] salt = BlockchainUtils.fromHex(storage.getSalt());
        SecretKey masterKey = SecurityUtils.deriveKey(password, salt);

        
        try {
            byte[] decryptedBlob = SecurityUtils.decryptAESGCM(
                masterKey,
                BlockchainUtils.fromHex(storage.getIv()),
                BlockchainUtils.fromHex(storage.getBlob())
            );

            
            User user = User.fromMap(JsonUtils.jsonToMap(BlockchainUtils.bytesToStr(decryptedBlob)));
            
            
            if (!user.getUsername().equals(username)) {
                throw new RuntimeException("Inconsistencia nos dados do usuario.");
            }

            return user;
        } catch (GeneralSecurityException e) {
            
            throw new RuntimeException("Senha incorreta.");
        }
    }
}
