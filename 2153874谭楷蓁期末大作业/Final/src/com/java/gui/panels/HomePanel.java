package com.java.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import com.java.gui.main.MainFrame;

public class HomePanel extends JPanel {
	
	private MainFrame mainFrame;
    private JTextField textField;

    public HomePanel(MainFrame mainFrame) {
    	this.mainFrame = mainFrame;

        setBackground(Color.WHITE); // 设置背景颜色
        setLayout(new BorderLayout(10, 10)); // 设置布局管理器

        // 中央面板
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(240, 240, 240));
        add(centerPanel, BorderLayout.CENTER);
        
        JLabel lblNewLabel = new JLabel("TKZ 题库系统");
        lblNewLabel.setBounds(162, 10, 208, 178);
        lblNewLabel.setIcon(new ImageIcon(MainFrame.class.getResource("/images/学士帽.png")));
        lblNewLabel.setFont(new Font("宋体", Font.BOLD, 20));
        
        textField = new JTextField();
        textField.setBounds(125, 199, 208, 21);
        textField.setForeground(Color.GRAY);
        textField.setText("请输入...");
        textField.setColumns(10);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals("请输入...")) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText("请输入...");
                }
            }
        });
        
        JButton btnNewButton = new JButton("检索");
        btnNewButton.setBounds(351, 198, 64, 23);
        btnNewButton.setFont(new Font("宋体", Font.BOLD, 12));
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (mainFrame != null) {
                    String searchQuery = textField.getText();
                    mainFrame.switchToBrowsePanel(searchQuery);
                } else {
                    System.out.println("MainFrame instance is not initialized.");
                }
            }
        });
        centerPanel.setLayout(null);
        centerPanel.add(lblNewLabel);
        centerPanel.add(textField);
        centerPanel.add(btnNewButton);
    }
}


