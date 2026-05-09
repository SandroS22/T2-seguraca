import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.spec.SecretKeySpec;

/**
 * Teste de Validação do Sistema de Logs e Trilhas de Auditoria.
 */
public class SystemLoggingTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Sistema de Logs (Atividade 5.1.3) ---");

            // 1. Limpar ambiente
            cleanup();
            File logFile = new File("MiniBlockchain/data/system.log");
            if (logFile.exists()) logFile.delete();

            // 2. Realizar Ações que geram Logs
            System.out.println("\n[SESSAO] Gerando eventos para o log...");
            
            // A. Registro
            String user = "logtester";
            String pass = "secret-pass-789";
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            String totp = (String) regRes.getData();

            // B. Login Falho (Senha)
            MiniBlockchainServer.loginStep1(user, "wrong-password");

            // C. Login Sucesso
            MiniBlockchainServer.loginStep1(user, pass);
            String code = TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totp), "HmacSHA256"), TotpService.getCurrentTimeStep());
            MiniBlockchainServer.loginStep2(code);

            // D. Operação de Blockchain
            MiniBlockchainServer.addBlock("Dados de log.");

            // E. Auditoria
            MiniBlockchainServer.audit();

            // F. Logout
            MiniBlockchainServer.logout();

            // 3. Validar arquivo de log
            System.out.println("\n[VALIDACAO] Verificando arquivo system.log...");
            if (logFile.exists()) {
                String content = new String(Files.readAllBytes(logFile.toPath()));
                System.out.println("--- CONTEUDO DO LOG ---");
                System.out.println(content);
                System.out.println("-----------------------");

                // Verificar presença de eventos chave
                boolean hasRegister = content.contains("Novo usuario registrado");
                boolean hasLoginFail = content.contains("Falha de autenticacao");
                boolean hasLoginSuccess = content.contains("Login Completo (MFA)");
                boolean hasBlock = content.contains("Novo bloco registrado");
                boolean hasAudit = content.contains("Auditoria concluida");

                if (hasRegister && hasLoginFail && hasLoginSuccess && hasBlock && hasAudit) {
                    System.out.println("[SUCESSO] Todos os eventos esperados foram registrados.");
                }

                // VERIFICACAO DE SEGURANCA: Garantir que a senha nao vazou
                if (!content.contains(pass)) {
                    System.out.println("[SUCESSO] Nenhuma senha em texto claro encontrada no log.");
                } else {
                    System.out.println("[FALHA] SENHA VAZADA NO LOG!");
                }
                
                if (!content.contains(totp)) {
                    System.out.println("[SUCESSO] Nenhum segredo TOTP encontrado no log.");
                }

            } else {
                System.out.println("[FALHA] Arquivo de log nao foi criado.");
            }

            System.out.println("\n--- Fim do Teste de Logs ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        File[] dirs = { new File("MiniBlockchain/data/blockchain"), new File("MiniBlockchain/data/users") };
        for (File dir : dirs) {
            if (dir.exists()) {
                for (File f : dir.listFiles()) f.delete();
            }
        }
    }
}
