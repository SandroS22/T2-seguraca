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
}
