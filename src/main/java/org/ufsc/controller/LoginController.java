package org.ufsc.controller;

import org.ufsc.view.MenuView;

import java.util.Map;

public class LoginController {

    public void Login() {
        Map<String, String> loginData = MenuView.ShowLogin();
        System.out.println(loginData);
    }
}
