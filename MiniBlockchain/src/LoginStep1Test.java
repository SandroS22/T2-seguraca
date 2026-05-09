import javax.crypto.SecretKey;

public class LoginStep1Test {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Login Estágio 1 (Atividade 2.2.1) ---");

            String user = "logintester";
            String pass = "p@ssword-123";

            // 1. Garantir que o usuário existe (Cadastro)
            System.out.println("Garantindo cadastro do usuario...");
            if (!StorageManager.userExists(user)) {
                AuthService.register(user, pass);
                System.out.println("Usuario cadastrado com sucesso.");
            }

            // 2. Teste de Autenticação com Senha Correta
            System.out.println("\nTentando login com SENHA CORRETA...");
            User authenticatedUser = AuthService.authenticateStep1(user, pass);
            System.out.println("[SUCESSO] Login aceito para: " + authenticatedUser.getUsername());
            
            // Simular o início da sessão (Passo 2)
            byte[] salt = BlockchainUtils.fromHex(StorageManager.loadUserStorage(user).getSalt());
            SecretKey sessionKey = SecurityUtils.deriveKey(pass, salt);
            SessionContext.setSession(authenticatedUser, sessionKey);
            System.out.println("Sessao iniciada. Chave de sessao em memoria: " + (SessionContext.isLoggedIn() ? "SIM" : "NAO"));

            // 3. Teste de Autenticação com Senha INCORRETA
            System.out.println("\nTentando login com SENHA INCORRETA...");
            try {
                AuthService.authenticateStep1(user, "senha-errada");
                System.out.println("[FALHA] O sistema aceitou uma senha errada!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Sistema rejeitou senha errada: " + e.getMessage());
            }

            // 4. Teste com Usuário Inexistente
            System.out.println("\nTentando login com USUARIO INEXISTENTE...");
            try {
                AuthService.authenticateStep1("nao-existo", "qualquer-senha");
                System.out.println("[FALHA] O sistema aceitou usuario inexistente!");
            } catch (Exception e) {
                System.out.println("[SUCESSO] Sistema rejeitou usuario inexistente: " + e.getMessage());
            }

            System.out.println("\n--- Fim do Teste de Login Estagio 1 ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
