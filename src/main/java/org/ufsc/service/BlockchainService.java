package org.ufsc.service;

import org.ufsc.model.Block;
import org.ufsc.model.User;
import org.ufsc.repository.BlockchainRepository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class BlockchainService {
    private final BlockchainRepository blockchainRepository = new BlockchainRepository();
    private final CryptoService cryptoService = new CryptoService();

    public void addBlock(User user, String message) throws Exception {
        String prevHash = blockchainRepository.getLastBlockHash();

        byte[] iv = CryptoService.generateIV();

        byte[] sessionKey = user.getPasswordHash();

        byte[] encryptedData = CryptoService.encrypt(
                message.getBytes(StandardCharsets.UTF_8),
                sessionKey,
                iv
        );

        Block newBlock = new Block(
                null,
                user.getUsername(),
                iv,
                encryptedData,
                prevHash,
                System.currentTimeMillis()
        );

        blockchainRepository.saveBlock(newBlock);
        System.out.println("Bloco adicionado com sucesso à blockchain!");
    }

    public void listAndValidateChain(User currentUser) {
        List<Block> chain = blockchainRepository.getAllBlocks();
        String expectedPrevHash = "0";

        System.out.println("\n--- EXPLORADOR DA BLOCKCHAIN ---");

        for (Block block : chain) {
            System.out.println("Bloco #" + block.getId() + " [Dono: " + block.getUsername() + "]");
            if (!block.getPrevHash().equals(expectedPrevHash)) {
                System.err.println("!!! ERRO DE INTEGRIDADE: Hash anterior inválido no bloco " + block.getId());
            }

            if (block.getUsername().equals(currentUser.getUsername())) {
                try {
                    byte[] decrypted = CryptoService.decrypt(
                            block.getEncryptedData(),
                            currentUser.getPasswordHash(),
                            block.getIv()
                    );
                    System.out.println(" > Mensagem: " + new String(decrypted, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    System.err.println(" > !!! FALHA DE AUTENTICIDADE: Os dados deste bloco foram alterados!");
                }
            } else {
                System.out.println(" > Mensagem: [CONTEÚDO CIFRADO]");
            }

            expectedPrevHash = CryptoService.calculateHash(Arrays.toString(block.getEncryptedData()));
            System.out.println("--------------------------------");
        }
    }

    public void corruptBlock(int id) {
        this.blockchainRepository.corruptBlockData(id);
    }
}