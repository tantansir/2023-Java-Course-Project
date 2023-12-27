package com.java.gui.panels;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.java.database.DatabaseConnection;
import com.java.gui.main.MainFrame;
import com.java.models.Question;
import com.java.models.User;

public class SetupPanel extends JPanel {
    private User currentUser;
    private int userId;
    private MainFrame mainFrame;
    
    private JLabel usernameLabel;
    private JLabel informationLabel;
    private JLabel emailLabel;
    private JButton editProfileButton;   // 修改个人资料按钮
    private JLabel userAvatar;           // 用户头像标签
    private JList<String> favoritesList;
    private JList<String> uploadsList;
    private Map<Integer, String> favoritesQuestionIdToContentMap = new HashMap<>();
    private Map<Integer, String> questionIdToContentMap = new HashMap<>();
    
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            updateFavoritesList(userId); // 当面板变为可见时，更新收藏列表
            updateUploadsList(userId); // 当面板变为可见时，更新上传列表
        }
    }

    public SetupPanel(MainFrame mainFrame, int userId) {
    	this.mainFrame = mainFrame;
        this.userId = userId;
        fetchUserData();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 头像和用户信息面板
        JPanel avatarAndInfoPanel = new JPanel();
        avatarAndInfoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // 头像
        userAvatar = new JLabel();
        userAvatar.setFont(new Font("宋体", Font.BOLD, 14));
        avatarAndInfoPanel.add(userAvatar);
        userAvatar.setIcon(new ImageIcon(SetupPanel.class.getResource("/images/avatar1.png")));
        userAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userAvatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("选择头像");
                fileChooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("图像文件", "jpg", "png", "gif");
                fileChooser.addChoosableFileFilter(filter);

                if (fileChooser.showOpenDialog(SetupPanel.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    ImageIcon icon = resizeIcon(file.getAbsolutePath(), 30, 30);
                    if (icon != null) {
                        userAvatar.setIcon(icon);
                        userAvatar.setPreferredSize(new Dimension(30, 30));
                        userAvatar.revalidate();
                    }
                }
            }
        });        

        // 用户信息面板
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        
        // 用户名
        usernameLabel = new JLabel(currentUser.getUsername());
        usernameLabel.setFont(new Font("宋体", Font.BOLD, 13));
        userInfoPanel.add(usernameLabel);

        // 基本信息
        informationLabel = new JLabel(currentUser.getInformation());
        informationLabel.setFont(new Font("宋体", Font.BOLD, 13));
        userInfoPanel.add(informationLabel);

        // 电子邮件
        emailLabel = new JLabel(currentUser.getEmail());
        emailLabel.setFont(new Font("宋体", Font.BOLD, 13));
        userInfoPanel.add(emailLabel);

        avatarAndInfoPanel.add(userInfoPanel);
        topPanel.add(avatarAndInfoPanel, BorderLayout.CENTER);
                
        // 修改个人资料按钮
        editProfileButton = new JButton("修改个人资料");
        editProfileButton.setFont(new Font("宋体", Font.PLAIN, 14));
        editProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 创建一个面板用于用户输入新信息
                JPanel panel = new JPanel(new GridLayout(0, 1));
                JLabel oldPasswordLabel = new JLabel("原密码:");
                JLabel oldPasswordValueLabel = new JLabel(currentUser.getPassword());
                JLabel passwordLabel = new JLabel("新密码:");
                JTextField passwordField = new JTextField(currentUser.getPassword());
                JLabel informationLabel = new JLabel("基本信息:");
                JTextField informationField = new JTextField(currentUser.getInformation());
                JLabel emailLabel = new JLabel("电子邮件:");
                JTextField emailField = new JTextField(currentUser.getEmail());

                panel.add(oldPasswordLabel);
                panel.add(oldPasswordValueLabel);
                panel.add(passwordLabel);
                panel.add(passwordField);
                panel.add(informationLabel);
                panel.add(informationField);
                panel.add(emailLabel);
                panel.add(emailField);

                // 显示对话框
                int result = JOptionPane.showConfirmDialog(null, panel, "修改个人资料",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String newPassword = new String(passwordField.getText());
                    if (newPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(SetupPanel.this, "新密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    String newInformation = informationField.getText();
                    String newEmail = emailField.getText();

                    // 在这里实现更新数据库中用户信息的逻辑
                    boolean updateSuccessful = updateUserInfo(userId, newPassword, newInformation, newEmail);

                    if (updateSuccessful) {
                        JOptionPane.showMessageDialog(SetupPanel.this, "个人资料更新成功！");

                        // 更新 currentUser 的信息
                        currentUser.setPassword(newPassword);
                        currentUser.setInformation(newInformation);
                        currentUser.setEmail(newEmail);
                        fetchUserData();

                    } else {
                        JOptionPane.showMessageDialog(SetupPanel.this, "更新失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });
        
        topPanel.add(editProfileButton, BorderLayout.EAST);   
        add(topPanel, BorderLayout.NORTH);

        // 收藏列表和上传列表
        JPanel favoritesPanel = new JPanel(new BorderLayout());
        JLabel favoritesLabel = new JLabel("我的收藏：");
        favoritesLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        favoritesList = new JList<>();
        favoritesPanel.add(favoritesLabel, BorderLayout.NORTH);
        favoritesPanel.add(new JScrollPane(favoritesList), BorderLayout.CENTER);

        JPanel uploadsPanel = new JPanel(new BorderLayout());
        JLabel uploadsLabel = new JLabel("我的上传：");
        uploadsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        uploadsList = new JList<>();
        uploadsPanel.add(uploadsLabel, BorderLayout.NORTH);
        uploadsPanel.add(new JScrollPane(uploadsList), BorderLayout.CENTER);
        
        updateFavoritesList(userId);
        updateUploadsList(userId);

        JPanel middlePanel = new JPanel(new GridLayout(2, 1));
        middlePanel.add(favoritesPanel);
        middlePanel.add(uploadsPanel);

        add(middlePanel, BorderLayout.CENTER);
        
        favoritesList.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                
                if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() == 1) {
                    int index = favoritesList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        favoritesList.setSelectedIndex(index);
                        showPopupMenu(evt.getComponent(), evt.getX(), evt.getY(), true, index);
                    }
                }
                
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    String selectedContent = (String) list.getModel().getElementAt(index);
                    int questionId = favoritesQuestionIdToContentMap.entrySet().stream()
                                            .filter(entry -> selectedContent.equals(entry.getValue()))
                                            .map(Map.Entry::getKey)
                                            .findFirst()
                                            .orElse(-1);
                    if (questionId != -1) {
                        showQuestionDetail(questionId);
                    }
                }
            }
        });

        uploadsList.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                
                if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() == 1) {
                    int index = uploadsList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        uploadsList.setSelectedIndex(index);
                        showPopupMenu(evt.getComponent(), evt.getX(), evt.getY(), false, index);
                    }
                }
                
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    String selectedContent = (String) list.getModel().getElementAt(index);
                    int questionId = questionIdToContentMap.entrySet().stream()
                                            .filter(entry -> selectedContent.equals(entry.getValue()))
                                            .map(Map.Entry::getKey)
                                            .findFirst()
                                            .orElse(-1);
                    if (questionId != -1) {
                        showQuestionDetail(questionId);
                    }
                }
            }
        });

    }
    
    private void fetchUserData() {
        // 从数据库获取用户信息
        String sql = "SELECT * FROM Users WHERE UserID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("UserID");
                String username = rs.getString("Username");
                String password = rs.getString("Password");
                String avatar = rs.getString("Avatar");
                String information = rs.getString("information");
                String email = rs.getString("Email");

                // 假设 User 类有相应的构造函数
                currentUser = new User(id, username, password, avatar, information, email);

                // 更新界面以显示用户信息
                SwingUtilities.invokeLater(() -> {
                    usernameLabel.setText("用户名: " + currentUser.getUsername());
                    informationLabel.setText("基本信息: " + currentUser.getInformation());
                    emailLabel.setText("电子邮件: " + currentUser.getEmail());
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateFavoritesList(int userId) {
        DefaultListModel<String> model = new DefaultListModel<>();
        String sql = "SELECT q.questionID, q.content FROM favorites f JOIN questions q ON f.questionID = q.questionID WHERE f.userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	 int questionId = rs.getInt("questionID");
                 String content = rs.getString("Content");
                 model.addElement(content);
                 favoritesQuestionIdToContentMap.put(questionId, content);
            }
            SwingUtilities.invokeLater(() -> favoritesList.setModel(model));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUploadsList(int userId) {
        DefaultListModel<String> model = new DefaultListModel<>();
        String sql = "SELECT questionID, content FROM questions WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	int questionId = rs.getInt("questionID");
                String content = rs.getString("Content");
                model.addElement(content);
                questionIdToContentMap.put(questionId, content);
            }
            SwingUtilities.invokeLater(() -> uploadsList.setModel(model));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean updateUserInfo(int userId, String password, String information, String email) {
        String sql = "UPDATE Users SET password = ?, information = ?, Email = ? WHERE UserID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
        	stmt.setString(1, password);
            stmt.setString(2, information);
            stmt.setString(3, email);
            stmt.setInt(4, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void showQuestionDetail(int questionId) {
        // 根据题目ID获取题目详情
        Question question = getQuestionFromDatabase(questionId);
        if (question != null) {
            mainFrame.showQuestionDetail(question, userId);
        }
    }

    private Question getQuestionFromDatabase(int questionId) {
        // 这里实现从数据库获取题目详情的逻辑
        String sql = "SELECT * FROM questions WHERE QuestionID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 假设 Question 类有相应的构造器
                    return new Question(
                        rs.getInt("QuestionID"),
                        rs.getString("Subject"),
                        rs.getString("Type"),
                        rs.getString("Difficulty"),
                        rs.getString("Content"),
                        rs.getString("Answer")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 设置头像大小的方法
    private ImageIcon resizeIcon(String path, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 显示弹出删除菜单
    private void showPopupMenu(Component component, int x, int y, boolean isFavorite, int index) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("删除");
        popupMenu.add(deleteItem);

        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFavorite) {
                    // 实现删除收藏的逻辑
                    int questionId = getQuestionIdFromFavorites(index);
                    if (questionId != -1) {
                        deleteFavorite(questionId);
                    }
                } else {
                    // 实现删除上传的题目的逻辑
                    int questionId = getQuestionIdFromUploads(index);
                    if (questionId != -1) {
                        deleteUpload(questionId);
                    }
                }
                updateFavoritesList(userId);
                updateUploadsList(userId);
            }
        });

        popupMenu.show(component, x, y);
    }
    
    private void deleteFavorite(int questionId) {
        // 实现删除收藏的数据库操作
        String sql = "DELETE FROM favorites WHERE questionID = ? AND userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteUpload(int questionId) {
        // 实现删除上传题目的数据库操作
    	String sqlDeleteUserQuestions = "DELETE FROM user_questions WHERE QuestionID = ?";
        String sqlDeleteMedia = "DELETE FROM questionmedia WHERE QuestionID = ?";
        String sqlDeleteChoices = "DELETE FROM question_choice WHERE QuestionID = ?";
        String sqlDeleteQuestion = "DELETE FROM questions WHERE questionID = ? AND userID = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // 关闭自动提交
            conn.setAutoCommit(false);

            // 先删除 user_questions 表中的相关条目
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteUserQuestions)) {
                stmt.setInt(1, questionId);
                stmt.executeUpdate();
            }

            // 再删除所有相关的媒体文件
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteMedia)) {
                stmt.setInt(1, questionId);
                stmt.executeUpdate();
            }

            // 再删除所有相关的选项
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteChoices)) {
                stmt.setInt(1, questionId);
                stmt.executeUpdate();
            }

            // 最后删除题目
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteQuestion)) {
                stmt.setInt(1, questionId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            // 提交事务
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            // 在这里处理错误，回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // 在 finally 块中关闭连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private int getQuestionIdFromFavorites(int index) {
        String selectedContent = favoritesList.getModel().getElementAt(index);
        return favoritesQuestionIdToContentMap.entrySet().stream()
               .filter(entry -> selectedContent.equals(entry.getValue()))
               .map(Map.Entry::getKey)
               .findFirst()
               .orElse(-1);
    }

    private int getQuestionIdFromUploads(int index) {
        String selectedContent = uploadsList.getModel().getElementAt(index);
        return questionIdToContentMap.entrySet().stream()
               .filter(entry -> selectedContent.equals(entry.getValue()))
               .map(Map.Entry::getKey)
               .findFirst()
               .orElse(-1);
    }

}

