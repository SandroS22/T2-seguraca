import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gerenciador de Armazenamento para o MiniBlockchain.
 * Responsável por persistir usuários e blocos em arquivos JSON.
 */
public class StorageManager {

    private static final String DATA_DIR = "MiniBlockchain/data";
    private static final String USERS_DIR = DATA_DIR + "/users";
    private static final String BLOCKCHAIN_DIR = DATA_DIR + "/blockchain";

    static {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
            Files.createDirectories(Paths.get(BLOCKCHAIN_DIR));
        } catch (IOException e) {
            System.err.println("Erro ao criar diretorios de dados: " + e.getMessage());
        }
    }

    // --- Operações de Usuário ---

    /**
     * Salva os dados cifrados do usuário (UserStorage).
     */
    public static void saveUserStorage(String username, UserStorage storage) throws IOException {
        String json = JsonUtils.mapToJson(storage.toMap());
        Path path = Paths.get(USERS_DIR, "user_" + username + ".json");
        Files.write(path, json.getBytes());
    }

    /**
     * Carrega os dados cifrados do usuário (UserStorage).
     */
    public static UserStorage loadUserStorage(String username) throws IOException {
        Path path = Paths.get(USERS_DIR, "user_" + username + ".json");
        if (!Files.exists(path)) return null;
        String json = new String(Files.readAllBytes(path));
        return UserStorage.fromMap(JsonUtils.jsonToMap(json));
    }

    public static boolean userExists(String username) {
        return Files.exists(Paths.get(USERS_DIR, "user_" + username + ".json"));
    }

    // --- Operações de Blockchain ---

    public static void saveBlock(Block block) throws IOException {
        String json = JsonUtils.mapToJson(block.toMap());
        // Nome do arquivo baseado no indice para manter a ordem
        String filename = String.format("block_%05d.json", Integer.parseInt(block.getIndex()));
        Path path = Paths.get(BLOCKCHAIN_DIR, filename);
        Files.write(path, json.getBytes());
    }

    public static List<Block> loadAllBlocks() throws IOException {
        List<Block> blocks = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(BLOCKCHAIN_DIR))) {
            List<Path> sortedPaths = paths
                .filter(p -> p.getFileName().toString().startsWith("block_"))
                .sorted()
                .collect(Collectors.toList());

            for (Path path : sortedPaths) {
                String json = new String(Files.readAllBytes(path));
                blocks.add(Block.fromMap(JsonUtils.jsonToMap(json)));
            }
        }
        return blocks;
    }

    public static Block getLastBlock() throws IOException {
        List<Block> blocks = loadAllBlocks();
        if (blocks.isEmpty()) return null;
        return blocks.get(blocks.size() - 1);
    }
}
