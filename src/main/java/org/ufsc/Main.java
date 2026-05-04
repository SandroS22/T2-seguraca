package org.ufsc;

import org.ufsc.controller.MainController;
import org.ufsc.util.MenuInput;
import org.ufsc.view.MenuView;

public class Main {
    public static void main(String[] args) {

        while(true) {
            int menuChoice = MenuView.ShowMenu();
            if(menuChoice == 0) {
                System.out.println("Encerrando...");
                System.exit(0);
            }
            MainController mainController = new MainController();
            mainController.mainMenu();
        }
    }
}