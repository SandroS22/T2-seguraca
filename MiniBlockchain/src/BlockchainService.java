import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.List;


public class BlockchainService {

    
    public static class BlockPayload {
        public final String dataEnc;
        public final String iv;

        public BlockPayload(String dataEnc, String iv) {
            this.dataEnc = dataEnc;
            this.iv = iv;
        }
    }

    
    public static BlockPayload encryptBlockPayload(String content) throws GeneralSecurityException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Usuario nao autenticado. Chave de sessao indisponivel.");
        }

        
        SecretKey sessionKey = SessionContext.getSessionKey();

        
        byte[] ivBytes = SecurityUtils.generateGcmIV();

        
        byte[] plaintext = BlockchainUtils.strToBytes(content);

        
        byte[] ciphertext = SecurityUtils.encryptAESGCM(sessionKey, ivBytes, plaintext);

        
        return new BlockPayload(
            BlockchainUtils.toHex(ciphertext),
            BlockchainUtils.toHex(ivBytes)
        );
    }

    
    public static String decryptBlockPayload(Block block) throws GeneralSecurityException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Nenhum usuario autenticado.");
        }

        String currentUser = SessionContext.getCurrentUser().getUsername();

        
        if (!block.getOwner().equals(currentUser)) {
            return "[CONTEÚDO CIFRADO - ACESSO NEGADO]";
        }

        
        SecretKey sessionKey = SessionContext.getSessionKey();
        byte[] iv = BlockchainUtils.fromHex(block.getIv());
        byte[] ciphertext = BlockchainUtils.fromHex(block.getDataEnc());

        
        try {
            byte[] decryptedBytes = SecurityUtils.decryptAESGCM(sessionKey, iv, ciphertext);
            return BlockchainUtils.bytesToStr(decryptedBytes);
        } catch (GeneralSecurityException e) {
            
            throw new GeneralSecurityException("Falha de integridade no bloco. O conteudo pode ter sido adulterado.", e);
        }
    }

    
    public static String calculateBlockHash(Block block) throws GeneralSecurityException {
        
        byte[] blockBytes = block.getBytesForHash();

        
        byte[] hashBytes = SecurityUtils.calculateSHA256(blockBytes);

        
        return BlockchainUtils.toHex(hashBytes);
    }

    
    public static ServerResponse verifyBlockFullIntegrity(Block block) {
        try {
            
            String recalculatedHash = calculateBlockHash(block);
            if (!recalculatedHash.equals(block.getHash())) {
                return ServerResponse.error("FALHA ESTRUTURAL: O hash do bloco nao confere com seu conteudo.");
            }

            
            
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

    
    public static void linkNewBlock(Block newBlock) throws java.io.IOException {
        Block lastBlock = StorageManager.getLastBlock();

        if (lastBlock == null) {
            
            newBlock.setIndex("0");
            newBlock.setHashPrev("0000000000000000000000000000000000000000000000000000000000000000");
        } else {
            
            int nextIndex = Integer.parseInt(lastBlock.getIndex()) + 1;
            newBlock.setIndex(String.valueOf(nextIndex));
            newBlock.setHashPrev(lastBlock.getHash());
        }
    }

    
    public static Block prepareNewBlock(String content) throws java.io.IOException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("Acesso negado. Usuario nao autenticado.");
        }

        Block block = new Block();
        
        
        block.setDataRaw(content);

        
        block.setTimestamp(String.valueOf(System.currentTimeMillis()));
        
        
        block.setOwner(SessionContext.getCurrentUser().getUsername());
        
        
        linkNewBlock(block);

        return block;
    }

    
    public static void sealBlock(Block block) throws java.security.GeneralSecurityException {
        if (block.getDataRaw() == null) {
            throw new IllegalStateException("Nao ha dados brutos para cifrar.");
        }

        
        BlockPayload payload = encryptBlockPayload(block.getDataRaw());
        
        
        block.setDataEnc(payload.dataEnc);
        block.setIv(payload.iv);

        
        block.setDataRaw(null);

        
        block.setHash(calculateBlockHash(block));
    }

    
    public static Block createAndSealBlock(String content) throws Exception {
        
        Block block = prepareNewBlock(content);

        
        sealBlock(block);

        return block;
    }

    
    public static ServerResponse auditFullChain() {
        try {
            List<Block> history = getBlockchainHistory();
            
            if (history.isEmpty()) {
                return ServerResponse.ok("Blockchain vazia e integra.", null);
            }

            for (int i = 0; i < history.size(); i++) {
                Block current = history.get(i);

                
                if (Integer.parseInt(current.getIndex()) != i) {
                    return ServerResponse.error("FALHA DE CONTINUIDADE: Bloco " + i + " ausente ou fora de ordem.");
                }

                
                String recalculatedHash = calculateBlockHash(current);
                if (!recalculatedHash.equals(current.getHash())) {
                    return ServerResponse.error("FALHA DE INTEGRIDADE: O selo do bloco #" + i + " foi violado.");
                }

                
                if (i == 0) {
                    
                    if (!current.getHashPrev().equals("0000000000000000000000000000000000000000000000000000000000000000")) {
                        return ServerResponse.error("FALHA DE GENESE: Bloco #0 possui vinculo invalido.");
                    }
                } else {
                    
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

    
    public static java.util.List<Block> getBlockchainHistory() throws java.io.IOException {
        return StorageManager.loadAllBlocks();
    }
}
