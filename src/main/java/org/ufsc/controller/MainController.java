package org.ufsc.controller;

import org.ufsc.util.MenuInput;

public class MainController {
    private LoginController loginController;

    public MainController() {
        this.loginController = new LoginController();
    }

    public void mainMenu() {
        this.loginController.Login();
    }

    public void handleMenuChoice(int choice) {
        switch (choice) {
            case 1:
                loginController.Login();
                break;
            case 2:
                loginController.Register();
                break;
            default:
                System.out.println("Opção inválida!");
                break;
        }
    }
}
