package org.ufsc;

import org.ufsc.controller.MainController;
import org.ufsc.repository.DatabaseConfig;
import org.ufsc.util.MenuInput;
import org.ufsc.view.MenuView;

public class Main {
    public static void main(String[] args) {

        DatabaseConfig.initialize();
        while (true) {
            int menuChoice = MenuView.showMenu();
            if (menuChoice == 0) {
                System.out.println("Encerrando...");
                MenuInput.clean();
                System.exit(0);
            }
            MainController mainController = new MainController();
            mainController.handleMenuChoice(menuChoice);
        }
    }
}