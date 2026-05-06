package org.ufsc.controller;

import org.ufsc.model.User;
import org.ufsc.service.BlockchainService;
import org.ufsc.util.MenuInput;

import java.util.Scanner;

public class BlockchainController {
    private final BlockchainService blockchainService = new BlockchainService();
    private final User currentUser;

    public BlockchainController(User user) {
        this.currentUser = user;
    }

    public void showUserMenu() {
        while (true) {
            System.out.println("\n--- MENU DA BLOCKCHAIN (Logado como: " + currentUser.getUsername() + ") ---");
            System.out.println("1 - Adicionar Bloco");
            System.out.println("2 - Listar Blockchain (Minhas mensagens)");
            System.out.println("0 - Sair");

            int choice = MenuInput.intInput();

            try {
                if (choice == 1) {
                    System.out.print("Digite a mensagem para o bloco: ");
                    String msg = MenuInput.stringInput();
                    blockchainService.addBlock(currentUser, msg);
                } else if (choice == 2) {
                    blockchainService.listAndValidateChain(currentUser);
                } else if (choice == 999) {
                    System.out.print("Digite o id do bloco a ser corrompido: ");
                    int id = MenuInput.intInput();
                    blockchainService.corruptBlock(id);
                } else if (choice == 0) {
                    break;
                }
            } catch (Exception e) {
                System.err.println("Erro na operação: " + e.getMessage());
            }
        }
    }
}