import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Serviço responsável pelas operações lógicas da Blockchain.
 * Coordena a cifragem, decifragem e integridade dos blocos.
 */
public class BlockchainService {

    /**
     * Encapsula o resultado da cifragem de um payload de bloco.
     */
    public static class BlockPayload {
        public final String dataEnc;
        public final String iv;

        public BlockPayload(String dataEnc, String iv) {
            this.dataEnc = dataEnc;
            this.iv = iv;
        }
    }

    /**
     * Cifra os dados para um novo bloco usando a chave de sessão atual.
     * Requisito 2.2: Dados cifrados individualmente com AES-GCM.
     */
    public static BlockPayload encryptBlockPayload(String content) throws GeneralSecurityException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Usuario nao autenticado. Chave de sessao indisponivel.");
        }

        // 1. Recuperar chave de sessão do servidor (em memória)
        SecretKey sessionKey = SessionContext.getSessionKey();

        // 2. Gerar IV único de 12 bytes para este bloco
        byte[] ivBytes = SecurityUtils.generateGcmIV();

        // 3. Converter conteúdo para bytes
        byte[] plaintext = BlockchainUtils.strToBytes(content);

        // 4. Executar cifragem AES-GCM
        byte[] ciphertext = SecurityUtils.encryptAESGCM(sessionKey, ivBytes, plaintext);

        // 5. Retornar payload formatado em Hex
        return new BlockPayload(
            BlockchainUtils.toHex(ciphertext),
            BlockchainUtils.toHex(ivBytes)
        );
    }

    /**
     * Decifra o conteúdo de um bloco apenas se o usuário logado for o dono.
     * Requisito 2.3: Usuário lista todos os blocos, mas só decifra os próprios.
     */
    public static String decryptBlockPayload(Block block) throws GeneralSecurityException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Nenhum usuario autenticado.");
        }

        String currentUser = SessionContext.getCurrentUser().getUsername();

        // 1. Verificação Lógica de Propriedade
        if (!block.getOwner().equals(currentUser)) {
            return "[CONTEÚDO CIFRADO - ACESSO NEGADO]";
        }

        // 2. Recuperar chave de sessão e metadados do bloco
        SecretKey sessionKey = SessionContext.getSessionKey();
        byte[] iv = BlockchainUtils.fromHex(block.getIv());
        byte[] ciphertext = BlockchainUtils.fromHex(block.getDataEnc());

        // 3. Executar decifragem (Valida integridade automaticamente via GCM)
        try {
            byte[] decryptedBytes = SecurityUtils.decryptAESGCM(sessionKey, iv, ciphertext);
            return BlockchainUtils.bytesToStr(decryptedBytes);
        } catch (GeneralSecurityException e) {
            // Se o GCM falhar, os dados foram adulterados no disco
            throw new GeneralSecurityException("Falha de integridade no bloco. O conteudo pode ter sido adulterado.", e);
        }
    }

    /**
     * Calcula o hash SHA-256 do bloco.
     * Atividade 3.2.1: Garante a imutabilidade da cadeia.
     */
    public static String calculateBlockHash(Block block) throws GeneralSecurityException {
        // 1. Obter a representação binária determinística do bloco
        byte[] blockBytes = block.getBytesForHash();

        // 2. Calcular o SHA-256 via Provedor FIPS
        byte[] hashBytes = SecurityUtils.calculateSHA256(blockBytes);

        // 3. Retornar representação Hex
        return BlockchainUtils.toHex(hashBytes);
    }

    /**
     * Realiza a verificação total de integridade do bloco (Estrutural e Criptográfica).
     * Atividade 3.2.3: Defesa em duas camadas.
     */
    public static ServerResponse verifyBlockFullIntegrity(Block block) {
        try {
            // 1. Verificação de Camada 1: Estrutural (SHA-256)
            String recalculatedHash = calculateBlockHash(block);
            if (!recalculatedHash.equals(block.getHash())) {
                return ServerResponse.error("FALHA ESTRUTURAL: O hash do bloco nao confere com seu conteudo.");
            }

            // 2. Verificação de Camada 2: Criptográfica (GCM Tag)
            // Só é possível validar se o usuário for o dono (possuir a chave de sessão correta)
            if (SessionContext.isLoggedIn() && block.getOwner().equals(SessionContext.getCurrentUser().getUsername())) {
                try {
                    decryptBlockPayload(block); 
                } catch (GeneralSecurityException e) {
                    Logger.security("Adulteracao detectada no bloco #" + block.getIndex() + " (Proprietario: " + block.getOwner() + ")");
                    return ServerResponse.error("FALHA CRIPTOGRAFICA: A integridade do dado cifrado foi violada (Tag GCM invalida).");
                }
            }

            return ServerResponse.ok("Integridade verificada com sucesso.", null);
        } catch (Exception e) {
            return ServerResponse.error("Erro durante auditoria: " + e.getMessage());
        }
    }

    /**
     * Vincula o novo bloco ao anterior na cadeia.
     * Atividade 3.2.2: Implementa o algoritmo de encadeamento.
     */
    public static void linkNewBlock(Block newBlock) throws java.io.IOException {
        Block lastBlock = StorageManager.getLastBlock();

        if (lastBlock == null) {
            // Caso Gênese: Primeiro bloco da rede
            newBlock.setIndex("0");
            newBlock.setHashPrev("0000000000000000000000000000000000000000000000000000000000000000");
        } else {
            // Caso Continuidade: Vincula ao hash do último bloco
            int nextIndex = Integer.parseInt(lastBlock.getIndex()) + 1;
            newBlock.setIndex(String.valueOf(nextIndex));
            newBlock.setHashPrev(lastBlock.getHash());
        }
    }

    /**
     * Inicializa um novo bloco com metadados e dados brutos.
     * Atividade 4.1.1: Coleta de Dados e Timestamping.
     */
    public static Block prepareNewBlock(String content) throws java.io.IOException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Acesso negado. Usuario nao autenticado.");
        }

        Block block = new Block();
        
        // 1. Atribuição de Dados Brutos (Temporário)
        block.setDataRaw(content);

        // 2. Atribuição de Timestamp (Milissegundos)
        block.setTimestamp(String.valueOf(System.currentTimeMillis()));
        
        // 3. Atribuição de Owner (Automático via Sessão)
        block.setOwner(SessionContext.getCurrentUser().getUsername());
        
        // 4. Encadeamento (Atribui Index e HashPrev)
        linkNewBlock(block);

        return block;
    }

    /**
     * Sela o bloco definitivamente (Cifra o dado e gera o Hash final).
     * Atividade 4.1.2: Cifragem e Cálculo de Hash no Registro.
     */
    public static void sealBlock(Block block) throws java.security.GeneralSecurityException {
        if (block.getDataRaw() == null) {
            throw new IllegalStateException("Nao ha dados brutos para cifrar.");
        }

        // 1. Cifragem do conteúdo bruto
        BlockPayload payload = encryptBlockPayload(block.getDataRaw());
        
        // 2. População dos campos criptográficos
        block.setDataEnc(payload.dataEnc);
        block.setIv(payload.iv);

        // 3. SECURE WIPE: Limpa o dado em texto claro imediatamente
        block.setDataRaw(null);

        // 4. Cálculo do Hash Final (Selo de Integridade)
        block.setHash(calculateBlockHash(block));
    }

    /**
     * Orquestra a criação completa de um bloco: Coleta -> Encadeamento -> Selagem.
     */
    public static Block createAndSealBlock(String content) throws Exception {
        // A. Preparação (Timestamp, Owner, Index, HashPrev)
        Block block = prepareNewBlock(content);

        // B. Selagem (AES-GCM e SHA-256)
        sealBlock(block);

        return block;
    }

    /**
     * Realiza a auditoria completa da blockchain (Estrutura e Vínculos).
     * Atividade 4.2.3: Validação de consistência de toda a cadeia.
     */
    public static ServerResponse auditFullChain() {
        try {
            List<Block> history = getBlockchainHistory();
            
            if (history.isEmpty()) {
                return ServerResponse.ok("Blockchain vazia e integra.", null);
            }

            for (int i = 0; i < history.size(); i++) {
                Block current = history.get(i);

                // 1. Verificar Continuidade de Índice (Furos)
                if (Integer.parseInt(current.getIndex()) != i) {
                    return ServerResponse.error("FALHA DE CONTINUIDADE: Bloco " + i + " ausente ou fora de ordem.");
                }

                // 2. Verificar Auto-Integridade (Hash do próprio bloco)
                String recalculatedHash = calculateBlockHash(current);
                if (!recalculatedHash.equals(current.getHash())) {
                    return ServerResponse.error("FALHA DE INTEGRIDADE: O selo do bloco #" + i + " foi violado.");
                }

                // 3. Verificar Vínculo com o Anterior (Encadeamento)
                if (i == 0) {
                    // Bloco Gênese deve ter 64 zeros
                    if (!current.getHashPrev().equals("0000000000000000000000000000000000000000000000000000000000000000")) {
                        return ServerResponse.error("FALHA DE GENESE: Bloco #0 possui vinculo invalido.");
                    }
                } else {
                    // Bloco N deve apontar para o Hash de N-1
                    String prevHash = history.get(i - 1).getHash();
                    if (!current.getHashPrev().equals(prevHash)) {
                        return ServerResponse.error("FALHA DE ENCADEAMENTO: O bloco #" + i + " nao esta vinculado corretamente ao #" + (i-1));
                    }
                }
            }

            return ServerResponse.ok("AUDITORIA CONCLUIDA: Blockchain integra e consistente.", null);

        } catch (Exception e) {
            return ServerResponse.error("Erro durante execucao da auditoria: " + e.getMessage());
        }
    }

    /**
     * Recupera todo o histórico da blockchain.
     * Atividade 4.2.1: Listagem completa de blocos.
     */
    public static java.util.List<Block> getBlockchainHistory() throws java.io.IOException {
        return StorageManager.loadAllBlocks();
    }
}
