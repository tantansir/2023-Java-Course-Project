package com.java.gui.dialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.java.database.DatabaseConnection;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;

    public RegisterDialog() {
        setTitle("注册新用户");
        setModal(true);
        setSize(440, 320);
        getContentPane().setLayout(null);

        // 用户名
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setBounds(99, 57, 80, 25);
        getContentPane().add(userLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(169, 57, 165, 25);
        getContentPane().add(usernameField);

        // 密码
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(99, 106, 80, 25);
        getContentPane().add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(169, 106, 165, 25);
        getContentPane().add(passwordField);

        // 邮箱
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setBounds(99, 152, 80, 25);
        getContentPane().add(emailLabel);

        emailField = new JTextField(20);
        emailField.setBounds(169, 152, 165, 25);
        getContentPane().add(emailField);

        // 注册按钮
        JButton registerButton = new JButton("注册");
        registerButton.setBounds(156, 212, 89, 40);
        getContentPane().add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();

        // 简单的验证
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 连接数据库并插入新用户
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Users (Username, Password, Email) VALUES (?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password); // 注意：实际应用中应对密码进行加密处理
                statement.setString(3, email);

                int affectedRows = statement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "注册成功！");
                    dispose(); // 关闭对话框
                } else {
                    JOptionPane.showMessageDialog(this, "注册失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "注册出错：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
