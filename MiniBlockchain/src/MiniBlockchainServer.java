import javax.crypto.SecretKey;
import java.util.List;


public class MiniBlockchainServer {

    
    private static User pendingUser;
    private static SecretKey pendingKey;

    
    public static ServerResponse register(String username, String password) {
        try {
            String totpSecret = AuthService.register(username, password);
            return ServerResponse.ok("Usuario registrado com sucesso. Configure seu 2FA.", totpSecret);
        } catch (Exception e) {
            return ServerResponse.error("Erro no cadastro: " + e.getMessage());
        }
    }

    
    public static ServerResponse loginStep1(String username, String password) {
        if (SessionContext.isLoggedIn()) {
            return ServerResponse.error("Sessao ja ativa. Realize logout para trocar de usuario.");
        }
        
        try {
            User user = AuthService.authenticateStep1(username, password);
            
            
            pendingUser = user;
            byte[] salt = BlockchainUtils.fromHex(StorageManager.loadUserStorage(username).getSalt());
            pendingKey = SecurityUtils.deriveKey(password, salt);

            Logger.info("Login Passo 1 (Senha) OK para: " + username);
            return ServerResponse.ok("Senha validada. Insira o codigo TOTP.", null);
        } catch (Exception e) {
            Logger.warn("Falha de autenticacao (Senha) para: " + username + " - Erro: " + e.getMessage());
            return ServerResponse.error("Falha na autenticacao: " + e.getMessage());
        }
    }

    public static ServerResponse loginStep2(String code) {
        if (pendingUser == null || pendingKey == null) {
            return ServerResponse.error("Fluxo de login invalido. Inicie pelo passo 1.");
        }

        String username = pendingUser.getUsername();
        try {
            
            byte[] secretBytes = BlockchainUtils.fromHex(pendingUser.getTotpSecretEnc());
            SecretKey totpSecretKey = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA256");

            if (TotpService.validateCode(totpSecretKey, code)) {
                
                SessionContext.setSession(pendingUser, pendingKey);
                
                Logger.security("Login Completo (MFA) realizado com sucesso para: " + username);

                
                pendingUser = null;
                pendingKey = null;

                return ServerResponse.ok("Login realizado com sucesso! Bem-vindo, " + SessionContext.getCurrentUser().getUsername(), null);
            } else {
                Logger.security("Falha no TOTP para o usuario: " + username);
                
                
                pendingUser = null;
                pendingKey = null;
                return ServerResponse.error("Codigo TOTP invalido. Para sua seguranca, reinicie o login.");
            }
        } catch (Exception e) {
            Logger.error("Erro tecnico no Login Passo 2: " + e.getMessage());
            pendingUser = null;
            pendingKey = null;
            return ServerResponse.error("Erro na validacao TOTP: " + e.getMessage());
        }
    }

    
    public static ServerResponse logout() {
        if (isAuthenticated()) {
            Logger.info("Sessao encerrada (Logout).");
        }
        SessionContext.clear();
        pendingUser = null;
        pendingKey = null;
        return ServerResponse.ok("Logout realizado. Chaves removidas da memoria.", null);
    }

    
    public static boolean isAuthenticated() {
        return SessionContext.isLoggedIn();
    }

    
    public static ServerResponse addBlock(String content) {
        if (!isAuthenticated()) return ServerResponse.error("Operacao nao permitida. Realize login.");
        
        try {
            
            Block sealedBlock = BlockchainService.createAndSealBlock(content);

            
            StorageManager.saveBlock(sealedBlock);

            Logger.info("Novo bloco registrado. Indice: " + sealedBlock.getIndex());

            return ServerResponse.ok("Bloco registrado com sucesso na blockchain.", sealedBlock.getHash());
        } catch (Exception e) {
            Logger.error("Erro ao registrar bloco: " + e.getMessage());
            return ServerResponse.error("Erro ao registrar bloco: " + e.getMessage());
        }
    }

    
    public static ServerResponse getBlockchain() {
        if (!isAuthenticated()) return ServerResponse.error("Operacao nao permitida. Realize login.");

        try {
            List<Block> history = BlockchainService.getBlockchainHistory();
            
            
            for (Block b : history) {
                try {
                    String decryptedContent = BlockchainService.decryptBlockPayload(b);
                    b.setDataRaw(decryptedContent);
                } catch (Exception e) {
                    
                    b.setDataRaw("[ERRO DE INTEGRIDADE: Conteudo corrompido]");
                }
            }

            return ServerResponse.ok("Blockchain recuperada com decifragem seletiva.", history);
        } catch (Exception e) {
            return ServerResponse.error("Erro ao carregar blockchain: " + e.getMessage());
        }
    }

    
    public static ServerResponse audit() {
        if (!isAuthenticated()) return ServerResponse.error("Operacao nao permitida. Realize login.");
        ServerResponse res = BlockchainService.auditFullChain();
        if (res.isSuccess()) {
            Logger.info("Auditoria concluida: Sistema integro.");
        } else {
            Logger.security("ALERTA DE AUDITORIA: " + res.getMessage());
        }
        return res;
    }

    
    private static void checkSession() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuario autenticado.");
        }
    }
}
