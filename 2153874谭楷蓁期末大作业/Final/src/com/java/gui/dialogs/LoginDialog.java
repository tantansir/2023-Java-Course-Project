package com.java.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.java.database.DatabaseConnection;

public class LoginDialog extends JFrame {
    private JTextField userField;
    private JPasswordField passwordField;
    private boolean isAuthenticated = false;
    
    public interface OnLoginSuccessListener {
        void onLoginSuccess(int userId);
    }
    
   	public boolean isUserAuthenticated() {
		return isAuthenticated;
	}

    private OnLoginSuccessListener loginSuccessListener;

    public LoginDialog(OnLoginSuccessListener listener) {
    	this.loginSuccessListener = listener;
    	
        setTitle("题库系统登录");
        setSize(440, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel userLabel = new JLabel("用户名:");
        userLabel.setBounds(100, 66, 80, 25);
        getContentPane().add(userLabel);

        userField = new JTextField(20);
        userField.setBounds(157, 66, 165, 25);
        getContentPane().add(userField);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(110, 101, 80, 25);
        getContentPane().add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(157, 101, 165, 25);
        getContentPane().add(passwordField);

        JButton loginButton = new JButton("登录");
        loginButton.setBounds(103, 165, 87, 37);
        getContentPane().add(loginButton);
        
        JButton registerButton = new JButton("注册");
        registerButton.setBounds(224, 165, 87, 37);
        getContentPane().add(registerButton);

		loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                char[] password = passwordField.getPassword();

			try (Connection connection = DatabaseConnection.getConnection()) {
	            String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
	
	            try (PreparedStatement statement = connection.prepareStatement(sql)) {
	                statement.setString(1, username);
	                statement.setString(2, new String(password));
	
	                try (ResultSet resultSet = statement.executeQuery()) {
	                    if (resultSet.next()) {
	                        isAuthenticated = true;
	                        JOptionPane.showMessageDialog(LoginDialog.this, "登录成功！");
	                        dispose(); // 关闭登录窗口
	                    } else {
	                        JOptionPane.showMessageDialog(LoginDialog.this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
	                    }
	                }
	            }
	        } catch (Exception ex) {
	            JOptionPane.showMessageDialog(LoginDialog.this, "登录出错：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
	        }
                
				if (isAuthenticated) {
		            try (Connection connection = DatabaseConnection.getConnection()) {
		                String sql = "SELECT UserID FROM Users WHERE Username = ?";
		                try (PreparedStatement statement = connection.prepareStatement(sql)) {
		                    statement.setString(1, username);
		                    try (ResultSet resultSet = statement.executeQuery()) {
		                        if (resultSet.next()) {
		                            int userId = resultSet.getInt("UserID");
		                            if (loginSuccessListener != null) {
		                                loginSuccessListener.onLoginSuccess(userId);
		                            }
		                        }
		                    }
		                }
		            } catch (Exception ex) {
		                // 错误处理
		            }
		        }
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 显示注册对话框
                RegisterDialog registerDialog = new RegisterDialog();
                registerDialog.setVisible(true);
            }
        }); 
    }
}
