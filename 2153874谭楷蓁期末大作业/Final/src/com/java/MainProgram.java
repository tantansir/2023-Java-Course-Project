package com.java;

import javax.swing.*;

import com.java.gui.dialogs.LoginDialog;
import com.java.gui.main.MainFrame;

public class MainProgram {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showLoginDialog();
            }
        });
    }

    private static void showLoginDialog() {
        // 创建并显示登录对话框
        LoginDialog loginDialog = new LoginDialog(new LoginDialog.OnLoginSuccessListener() {
            @Override
            public void onLoginSuccess(int userId) {
                // 在登录成功时显示主窗口
                MainFrame mainFrame = new MainFrame(userId);
                mainFrame.setVisible(true);
            }
        });
        loginDialog.setVisible(true);
    }
}
