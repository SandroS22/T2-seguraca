import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;


public class IntegrityStressTest {
    public static void main(String[] args) {
        try {
            System.out.println("--- INICIANDO TESTE DE ESTRESSE DE INTEGRIDADE (5.2.2) ---");

            String user = "admin";
            String pass = "admin-strong-pass-123";

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 1] Modificar 1 bit do dado cifrado: ");
            tamperField(1, "dataEnc");
            ServerResponse res1 = MiniBlockchainServer.getBlockchain();
            validateSelectiveFailure(res1, 1, "[ERRO DE INTEGRIDADE]");

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 2] Modificar o IV do bloco: ");
            tamperField(2, "iv");
            ServerResponse res2 = MiniBlockchainServer.getBlockchain();
            validateSelectiveFailure(res2, 2, "[ERRO DE INTEGRIDADE]");

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 3] Modificar o HashPrev (Ponteiro): ");
            tamperField(2, "hashPrev");
            ServerResponse res3 = MiniBlockchainServer.audit();
            assertFail(res3, "FALHA DE ENCADEAMENTO");

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 4] Trocar indice do bloco 1 para 99: ");
            tamperField(1, "index", "99");
            ServerResponse res4 = MiniBlockchainServer.audit();
            assertFail(res4, "FALHA DE CONTINUIDADE");

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 5] Alterar dado + Recalcular Hash (Bypass Camada 1): ");
            tamperAndRecalculate(1);
            ServerResponse res5 = MiniBlockchainServer.getBlockchain();
            validateSelectiveFailure(res5, 1, "ERRO DE INTEGRIDADE");

            
            setupHealthyChain(user, pass, 3);
            System.out.print("[Ataque 6] Adulterar o vinculo do Bloco Gênese (#0): ");
            tamperField(0, "hashPrev", "falso-vinculo-123");
            ServerResponse res6 = MiniBlockchainServer.audit();
            assertFail(res6, "FALHA DE GENESE");

            validateLogs();

            System.out.println("\n--- TESTE DE ESTRESSE CONCLUIDO ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupHealthyChain(String user, String pass, int count) throws Exception {
        cleanup();
        String totp = (String) MiniBlockchainServer.register(user, pass).getData();
        MiniBlockchainServer.loginStep1(user, pass);
        MiniBlockchainServer.loginStep2(TotpService.calculateTOTP(new SecretKeySpec(BlockchainUtils.fromHex(totp), "HmacSHA256"), TotpService.getCurrentTimeStep()));
        for (int i = 0; i < count; i++) {
            MiniBlockchainServer.addBlock("Conteudo integro #" + i);
        }
    }

    private static void tamperField(int index, String fieldName) throws Exception {
        tamperField(index, fieldName, null);
    }

    private static void tamperField(int index, String fieldName, String newValue) throws Exception {
        Path path = Paths.get("MiniBlockchain/data/blockchain/block_" + String.format("%05d", index) + ".json");
        String json = new String(Files.readAllBytes(path));
        Map<String, String> map = JsonUtils.jsonToMap(json);
        
        if (newValue == null) {
            String original = map.get(fieldName);
            newValue = (original.charAt(0) == '0' ? '1' : '0') + original.substring(1);
        }
        
        map.put(fieldName, newValue);
        Files.write(path, JsonUtils.mapToJson(map).getBytes());
    }

    private static void tamperAndRecalculate(int index) throws Exception {
        Path path = Paths.get("MiniBlockchain/data/blockchain/block_" + String.format("%05d", index) + ".json");
        String json = new String(Files.readAllBytes(path));
        Map<String, String> map = JsonUtils.jsonToMap(json);
        
        
        String data = map.get("dataEnc");
        map.put("dataEnc", (data.charAt(0) == '0' ? 'a' : '0') + data.substring(1));
        
        
        Block b = Block.fromMap(map);
        map.put("hash", BlockchainService.calculateBlockHash(b));
        
        Files.write(path, JsonUtils.mapToJson(map).getBytes());
    }

    private static void validateSelectiveFailure(ServerResponse res, int expectedIndex, String expectedError) {
        List<Block> chain = (List<Block>) res.getData();
        Block target = null;
        for (Block b : chain) {
            if (Integer.parseInt(b.getIndex()) == expectedIndex) {
                target = b;
                break;
            }
        }

        if (target != null) {
            String content = target.getDataRaw();
            
            if (content != null && content.toUpperCase().contains("ERRO DE INTEGRIDADE")) {
                System.out.println("PASS (Sistema detectou a falha no bloco #" + expectedIndex + ")");
            } else {
                System.out.println("FAIL (Sistema nao detectou: " + expectedError + " | Obtido: " + content + ")");
            }
        } else {
            System.out.println("FAIL (Bloco #" + expectedIndex + " nao encontrado na resposta!)");
        }
    }

    private static void assertFail(ServerResponse res, String expectedMsg) {
        if (!res.isSuccess() && res.getMessage().toUpperCase().contains(expectedMsg.toUpperCase())) {
            System.out.println("PASS (" + res.getMessage() + ")");
        } else {
            if (!res.isSuccess() && res.getMessage().contains("FALHA DE INTEGRIDADE")) {
                System.out.println("PASS (Integridade detectou a quebra do vinculo)");
            } else {
                System.out.println("FAIL (Obtido: " + res.getMessage() + ")");
            }
        }
    }

    private static void validateLogs() throws Exception {
        System.out.println("\n[VALIDACAO DE LOGS] Verificando system.log...");
        File logFile = new File("MiniBlockchain/data/system.log");
        if (logFile.exists()) {
            String content = new String(Files.readAllBytes(logFile.toPath()));
            if (content.contains("[SECURITY]")) {
                System.out.println("[OK] Eventos de seguranca registrados.");
            } else {
                System.out.println("[ERRO] Nenhum log de seguranca encontrado!");
            }
        }
    }

    private static void cleanup() {
        File[] dirs = { new File("MiniBlockchain/data/blockchain"), new File("MiniBlockchain/data/users") };
        for (File dir : dirs) {
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) for (File f : files) f.delete();
            }
        }
    }
}
