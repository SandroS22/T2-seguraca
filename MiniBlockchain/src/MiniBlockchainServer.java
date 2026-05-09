import javax.crypto.SecretKey;
import java.util.List;

/**
 * Fachada (Facade) que representa o "Servidor" da MiniBlockchain.
 * Ponto único de interação para o Cliente (CLI).
 * Cumpre o requisito 6.i de isolamento entre cliente e servidor.
 */
public class MiniBlockchainServer {

    // Estado temporário durante o processo de login em 2 passos
    private static User pendingUser;
    private static SecretKey pendingKey;

    /**
     * Realiza o registro de um novo usuário.
     */
    public static ServerResponse register(String username, String password) {
        try {
            String totpSecret = AuthService.register(username, password);
            return ServerResponse.ok("Usuario registrado com sucesso. Configure seu 2FA.", totpSecret);
        } catch (Exception e) {
            return ServerResponse.error("Erro no cadastro: " + e.getMessage());
        }
    }

    /**
     * Login Estágio 1: Validação de Senha.
     */
    public static ServerResponse loginStep1(String username, String password) {
        if (SessionContext.isLoggedIn()) {
            return ServerResponse.error("Sessao ja ativa. Realize logout para trocar de usuario.");
        }
        
        try {
            User user = AuthService.authenticateStep1(username, password);
            
            // Mantém os dados em memória temporária do "Servidor" para o próximo passo
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
            // Extrair o segredo TOTP decifrado (está em texto claro no objeto User recuperado no Passo 1)
            byte[] secretBytes = BlockchainUtils.fromHex(pendingUser.getTotpSecretEnc());
            SecretKey totpSecretKey = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA256");

            if (TotpService.validateCode(totpSecretKey, code)) {
                // Autenticação completa: Estabelece a sessão definitiva
                SessionContext.setSession(pendingUser, pendingKey);
                
                Logger.security("Login Completo (MFA) realizado com sucesso para: " + username);

                // Limpa estado temporário de sucesso
                pendingUser = null;
                pendingKey = null;

                return ServerResponse.ok("Login realizado com sucesso! Bem-vindo, " + SessionContext.getCurrentUser().getUsername(), null);
            } else {
                Logger.security("Falha no TOTP para o usuario: " + username);
                // SEGURANÇA: Limpa o estado pendente em caso de falha no TOTP
                // Força o usuário a fornecer a senha novamente (evita brute force do código de 6 dígitos)
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

    /**
     * Encerra a sessão atual e limpa todo o estado interno.
     * Realiza um "Secure Wipe" de referências em memória.
     */
    public static ServerResponse logout() {
        if (isAuthenticated()) {
            Logger.info("Sessao encerrada (Logout).");
        }
        SessionContext.clear();
        pendingUser = null;
        pendingKey = null;
        return ServerResponse.ok("Logout realizado. Chaves removidas da memoria.", null);
    }

    /**
     * Verifica se há uma sessão ativa.
     */
    public static boolean isAuthenticated() {
        return SessionContext.isLoggedIn();
    }

    /**
     * Adiciona um novo bloco à blockchain.
     * Exige sessão ativa.
     */
    public static ServerResponse addBlock(String content) {
        if (!isAuthenticated()) return ServerResponse.error("Operacao nao permitida. Realize login.");
        
        try {
            // 1. Criar e Selar o bloco (Coleta + Encadeamento + Cifragem + Hashing)
            Block sealedBlock = BlockchainService.createAndSealBlock(content);

            // 2. Persistir no disco
            StorageManager.saveBlock(sealedBlock);

            Logger.info("Novo bloco registrado. Indice: " + sealedBlock.getIndex());

            return ServerResponse.ok("Bloco registrado com sucesso na blockchain.", sealedBlock.getHash());
        } catch (Exception e) {
            Logger.error("Erro ao registrar bloco: " + e.getMessage());
            return ServerResponse.error("Erro ao registrar bloco: " + e.getMessage());
        }
    }

    /**
     * Recupera a lista de blocos da blockchain com decifragem seletiva.
     * Atividade 4.2.2: Filtro de acesso e decifragem para o dono.
     */
    public static ServerResponse getBlockchain() {
        if (!isAuthenticated()) return ServerResponse.error("Operacao nao permitida. Realize login.");

        try {
            List<Block> history = BlockchainService.getBlockchainHistory();
            
            // Realizar processamento de decifragem seletiva (Server-Side)
            for (Block b : history) {
                try {
                    String decryptedContent = BlockchainService.decryptBlockPayload(b);
                    b.setDataRaw(decryptedContent);
                } catch (Exception e) {
                    // Se falhar a integridade GCM, reportamos no campo de dados
                    b.setDataRaw("[ERRO DE INTEGRIDADE: Conteudo corrompido]");
                }
            }

            return ServerResponse.ok("Blockchain recuperada com decifragem seletiva.", history);
        } catch (Exception e) {
            return ServerResponse.error("Erro ao carregar blockchain: " + e.getMessage());
        }
    }

    /**
     * Realiza a auditoria estrutural completa da blockchain.
     * Atividade 4.2.3: Validação de consistência.
     */
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

    /**
     * Helper privado para garantir que apenas o servidor manipule a sessao.
     */
    private static void checkSession() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuario autenticado.");
        }
    }
}
