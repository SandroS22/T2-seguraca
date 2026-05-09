import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Serviço de Autenticação e Gestão de Usuários.
 * Implementa a lógica de Cadastro e futuramente de Login.
 */
public class AuthService {

    /**
     * Registra um novo usuário no sistema.
     * Retorna o segredo TOTP (Hex) para o usuário configurar seu autenticador.
     */
    public static String register(String username, String password) throws GeneralSecurityException, IOException {
        // Validações de Regras de Negócio (Atividade 1.2.1)
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

        // 1. Gerar Salt unico
        byte[] salt = SecurityUtils.generateRandomBytes(16);
        String saltHex = BlockchainUtils.toHex(salt);

        // 2. Derivar Chave Mestra (KDF)
        SecretKey masterKey = SecurityUtils.deriveKey(password, salt);

        // 3. Criar Hash de Verificação de Senha
        byte[] verifier = SecurityUtils.calculateHMAC(masterKey, BlockchainUtils.strToBytes("auth-verifier"));
        String passwordHash = BlockchainUtils.toHex(verifier);

        // 4. Gerar Segredo TOTP real
        SecretKey totpSecret = SecurityUtils.generateTotpSecret();
        String totpHex = BlockchainUtils.toHex(totpSecret.getEncoded());

        // 5. Criar Objeto User em memoria
        User newUser = new User(username, saltHex, passwordHash, totpHex, "N/A");

        // 6. Cifrar o objeto User para persistencia (Envelope Cifrado)
        byte[] iv = SecurityUtils.generateGcmIV();
        byte[] userJsonBytes = BlockchainUtils.strToBytes(JsonUtils.mapToJson(newUser.toMap()));
        byte[] blob = SecurityUtils.encryptAESGCM(masterKey, iv, userJsonBytes);

        UserStorage storage = new UserStorage(
            saltHex,
            BlockchainUtils.toHex(iv),
            BlockchainUtils.toHex(blob)
        );

        // 7. Salvar via StorageManager de forma segura
        StorageManager.saveUserStorage(username, storage);

        Logger.info("Novo usuario registrado: " + username);

        // Retorna em Base32 para compatibilidade com Google Authenticator/2FAS
        return BlockchainUtils.toBase32(totpSecret.getEncoded()); 
    }

    /**
     * Primeiro estágio da autenticação: Verificação da Senha.
     * Tenta decifrar o envelope do usuário. Se falhar, a senha está incorreta.
     * Retorna o objeto User (contendo o segredo TOTP) em caso de sucesso.
     */
    public static User authenticateStep1(String username, String password) throws GeneralSecurityException, IOException {
        UserStorage storage = StorageManager.loadUserStorage(username);
        if (storage == null) {
            throw new RuntimeException("Usuario nao encontrado: " + username);
        }

        // 1. Derivar a chave a partir da senha fornecida e do salt público
        byte[] salt = BlockchainUtils.fromHex(storage.getSalt());
        SecretKey masterKey = SecurityUtils.deriveKey(password, salt);

        // 2. Tentar decifrar o blob (Envelope Cifrado)
        try {
            byte[] decryptedBlob = SecurityUtils.decryptAESGCM(
                masterKey,
                BlockchainUtils.fromHex(storage.getIv()),
                BlockchainUtils.fromHex(storage.getBlob())
            );

            // 3. Converter JSON decifrado de volta para objeto User
            User user = User.fromMap(JsonUtils.jsonToMap(BlockchainUtils.bytesToStr(decryptedBlob)));
            
            // Garantimos que o username interno coincide (sanidade)
            if (!user.getUsername().equals(username)) {
                throw new RuntimeException("Inconsistencia nos dados do usuario.");
            }

            return user;
        } catch (GeneralSecurityException e) {
            // Se o GCM falhar, a tag de autenticação não bate (senha errada ou arquivo corrompido)
            throw new RuntimeException("Senha incorreta.");
        }
    }
}
