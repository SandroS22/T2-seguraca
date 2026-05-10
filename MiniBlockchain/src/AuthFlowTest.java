import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;


public class AuthFlowTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- INICIANDO SUITE DE TESTES DE AUTENTICACAO (5.2.1) ---");

            
            cleanup();

            String user = "tester";
            String pass = "p@ssword123";

            
            System.out.print("[Cenario 1] Sucesso Total: ");
            ServerResponse reg = MiniBlockchainServer.register(user, pass);
            String totpSec = (String) reg.getData();
            MiniBlockchainServer.loginStep1(user, pass);
            String code = TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totpSec), "HmacSHA256"), TotpService.getCurrentTimeStep());
            ServerResponse res1 = MiniBlockchainServer.loginStep2(code);
            assertSuccess(res1, "Login OK");
            MiniBlockchainServer.logout();

            
            System.out.print("[Cenario 2] Usuario Inexistente: ");
            ServerResponse res2 = MiniBlockchainServer.loginStep1("ghost", pass);
            assertError(res2, "Usuario nao encontrado");

            
            System.out.print("[Cenario 3] Senha Errada: ");
            ServerResponse res3 = MiniBlockchainServer.loginStep1(user, "wrong-pass");
            assertError(res3, "Senha incorreta");

            
            System.out.print("[Cenario 4] TOTP Errado: ");
            MiniBlockchainServer.loginStep1(user, pass);
            ServerResponse res4 = MiniBlockchainServer.loginStep2("000000"); 
            assertError(res4, "Codigo TOTP invalido");
            
            
            ServerResponse res4b = MiniBlockchainServer.loginStep2(code);
            if (!res4b.isSuccess() && res4b.getMessage().contains("Inicie pelo passo 1")) {
                System.out.println("PASS (Estado limpo com sucesso)");
            } else {
                System.out.println("FAIL (Estado nao foi limpo!)");
            }

            
            System.out.print("[Cenario 5] Bypass de Estagio (Pular Senha): ");
            ServerResponse res5 = MiniBlockchainServer.loginStep2(code);
            assertError(res5, "Fluxo de login invalido");

            
            System.out.print("[Cenario 6] Sobrescrita de Sessao: ");
            
            MiniBlockchainServer.loginStep1(user, pass);
            MiniBlockchainServer.loginStep2(code);
            
            MiniBlockchainServer.register("bob", "bob-pass-123");
            ServerResponse res6 = MiniBlockchainServer.loginStep1("bob", "bob-pass-123");
            
            if (!res6.isSuccess() && res6.getMessage().contains("Sessao ja ativa")) {
                System.out.println("PASS (Bloqueou inicio de novo login)");
            } else {
                System.out.println("FAIL (Nao barrou login concorrente)");
            }

            
            System.out.println("\n[PASSO 3] Verificando Auditoria de Logs...");
            File logFile = new File("MiniBlockchain/data/system.log");
            if (logFile.exists()) {
                String logContent = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
                if (logContent.contains("[SECURITY]")) {
                    System.out.println("[SUCESSO] Logs de seguranca encontrados.");
                } else {
                    System.out.println("[FALHA] Nenhum evento SECURITY registrado!");
                }
            }

            System.out.println("\n--- SUITE DE TESTES CONCLUIDA ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void assertSuccess(ServerResponse res, String msg) {
        if (res.isSuccess()) {
            System.out.println("PASS (" + res.getMessage() + ")");
        } else {
            System.out.println("FAIL (" + res.getMessage() + ")");
        }
    }

    private static void assertError(ServerResponse res, String expectedPart) {
        if (!res.isSuccess() && res.getMessage().contains(expectedPart)) {
            System.out.println("PASS (" + res.getMessage() + ")");
        } else {
            System.out.println("FAIL (Esperava: " + expectedPart + " | Obtido: " + res.getMessage() + ")");
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
