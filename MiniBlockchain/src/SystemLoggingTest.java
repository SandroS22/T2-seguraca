import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.spec.SecretKeySpec;


public class SystemLoggingTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- Teste de Sistema de Logs (Atividade 5.1.3) ---");

            
            cleanup();
            File logFile = new File("MiniBlockchain/data/system.log");
            if (logFile.exists()) logFile.delete();

            
            System.out.println("\n[SESSAO] Gerando eventos para o log...");
            
            
            String user = "logtester";
            String pass = "secret-pass-789";
            ServerResponse regRes = MiniBlockchainServer.register(user, pass);
            String totp = (String) regRes.getData();

            
            MiniBlockchainServer.loginStep1(user, "wrong-password");

            
            MiniBlockchainServer.loginStep1(user, pass);
            String code = TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totp), "HmacSHA256"), TotpService.getCurrentTimeStep());
            MiniBlockchainServer.loginStep2(code);

            
            MiniBlockchainServer.addBlock("Dados de log.");

            
            MiniBlockchainServer.audit();

            
            MiniBlockchainServer.logout();

            
            System.out.println("\n[VALIDACAO] Verificando arquivo system.log...");
            if (logFile.exists()) {
                String content = new String(Files.readAllBytes(logFile.toPath()));
                System.out.println("--- CONTEUDO DO LOG ---");
                System.out.println(content);
                System.out.println("-----------------------");

                
                boolean hasRegister = content.contains("Novo usuario registrado");
                boolean hasLoginFail = content.contains("Falha de autenticacao");
                boolean hasLoginSuccess = content.contains("Login Completo (MFA)");
                boolean hasBlock = content.contains("Novo bloco registrado");
                boolean hasAudit = content.contains("Auditoria concluida");

                if (hasRegister && hasLoginFail && hasLoginSuccess && hasBlock && hasAudit) {
                    System.out.println("[SUCESSO] Todos os eventos esperados foram registrados.");
                }

                
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
