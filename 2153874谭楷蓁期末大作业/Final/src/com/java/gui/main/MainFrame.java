package com.java.gui.main;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout.Alignment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.LayoutStyle.ComponentPlacement;
import com.java.gui.panels.HomePanel;
import com.java.gui.panels.QuestionDetailPanel;
import com.java.gui.dialogs.LoginDialog;
import com.java.gui.panels.BrowsePanel;
import com.java.gui.panels.SetupPanel;
import com.java.gui.panels.UploadPanel;
import com.java.models.Question;
import com.java.models.User;

public class MainFrame extends JFrame {
	// 卡片名称常量
    private final static String HOME_PANEL = "Home Panel";
    private final static String BROWSE_PANEL = "Browse Panel";
    private final static String UPLOAD_PANEL = "Upload Panel";
    private final static String SETUP_PANEL = "Setup Panel";
    private final static String DETAIL_PANEL = "Detail Panel";
    private BrowsePanel browsePanel;
    private HomePanel homePanel;
    private JPanel cards;
    private int userId;
  
    public MainFrame(int userId) {
    	
    	this.userId = userId;
        setTitle("TKZ 题库系统");
        setSize(680, 478);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(240, 240, 240)); // 设置背景颜色为白色
        getContentPane().setLayout(new BorderLayout(10, 10)); // 添加边距
        	
        // 创建卡片布局面板
        cards = new JPanel(new CardLayout()); // 正确初始化类成员变量
        homePanel = new HomePanel(this);  // 创建首页面板
        browsePanel = new BrowsePanel(this, userId); // 创建浏览题目面板
        JPanel uploadPanel = new UploadPanel(userId); // 创建上传题目面板
        JPanel setupPanel = new SetupPanel(this, userId); // 创建设置面板

        // 向卡片布局面板添加面板
        cards.add(homePanel, HOME_PANEL);
        cards.add(browsePanel, BROWSE_PANEL);
        cards.add(uploadPanel, UPLOAD_PANEL);
        cards.add(setupPanel, SETUP_PANEL);
        cards.add(new JPanel(), DETAIL_PANEL);
        
        // 创建侧边栏
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 0, 0)); // 使按钮等宽并四等分侧边栏
        sidebar.setBackground(new Color(230, 240, 250)); // 设置背景颜色
        
        JButton button_home = new JButton("首页");
        button_home.setFont(new Font("宋体", Font.BOLD, 14));
        button_home.setIcon(new ImageIcon(MainFrame.class.getResource("/images/搜索.png")));
        button_home.setBackground(new Color(200, 220, 240)); // 设置按钮背景颜色
        button_home.setForeground(Color.BLACK); // 设置文字颜色
        button_home.setFocusPainted(false); // 去除焦点边框
        button_home.setBorderPainted(false); // 去除边框
        sidebar.add(button_home);
        
        JButton button_browse = new JButton("浏览");
        button_browse.setIcon(new ImageIcon(MainFrame.class.getResource("/images/教学楼.png")));
        button_browse.setFont(new Font("宋体", Font.BOLD, 14));
        button_browse.setBackground(new Color(200, 220, 240)); // 设置按钮背景颜色
        button_browse.setForeground(Color.BLACK); // 设置文字颜色
        button_browse.setFocusPainted(false); // 去除焦点边框
        button_browse.setBorderPainted(false); // 去除边框
        sidebar.add(button_browse);
        
        JButton button_upload = new JButton("上传");
        button_upload.setIcon(new ImageIcon(MainFrame.class.getResource("/images/纸飞机.png")));
        button_upload.setFont(new Font("宋体", Font.BOLD, 14));
        button_upload.setBackground(new Color(200, 220, 240)); // 设置按钮背景颜色
        button_upload.setForeground(Color.BLACK); // 设置文字颜色
        button_upload.setFocusPainted(false); // 去除焦点边框
        button_upload.setBorderPainted(false); // 去除边框
        sidebar.add(button_upload);
        
        JButton button_setup = new JButton("设置");
        button_setup.setIcon(new ImageIcon(MainFrame.class.getResource("/images/文件.png")));
        button_setup.setFont(new Font("宋体", Font.BOLD, 14));
        button_setup.setBackground(new Color(200, 220, 240)); // 设置按钮背景颜色
        button_setup.setForeground(Color.BLACK); // 设置文字颜色
        button_setup.setFocusPainted(false); // 去除焦点边框
        button_setup.setBorderPainted(false); // 去除边框
        sidebar.add(button_setup);

        // 在按钮的事件监听器中切换面板
        button_home.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, HOME_PANEL);
            }
        });
        
        button_browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, BROWSE_PANEL);
            }
        });

        button_upload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, UPLOAD_PANEL);
            }
        });

        button_setup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(cards.getLayout());
                cl.show(cards, SETUP_PANEL);
            }
        });

        // 创建底部设计者标识
        JLabel designedByLabel = new JLabel("Designed by TKZ", SwingConstants.RIGHT);
        designedByLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        designedByLabel.setBorder(new EmptyBorder(0, 0, 10, 10)); // 添加边距

        // 将组件添加到框架
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(cards, BorderLayout.CENTER);
        getContentPane().add(designedByLabel, BorderLayout.SOUTH);
        

    }
    public void switchToBrowsePanel(String searchQuery) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, BROWSE_PANEL);
        browsePanel.performSearch(searchQuery);
    }
    
    public void BackToBrowsePanel() {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, BROWSE_PANEL);
    }
    
    public void showQuestionDetail(Question question, int userId) {
        QuestionDetailPanel detailPanel = new QuestionDetailPanel(question, userId, this);
        cards.add(detailPanel, DETAIL_PANEL); // 替换旧的题目详情面板
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, DETAIL_PANEL); // 显示题目详情面板
    }

}