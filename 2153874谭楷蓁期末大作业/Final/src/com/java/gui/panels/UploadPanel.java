package com.java.gui.panels;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;

import com.java.database.DatabaseConnection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UploadPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private DefaultListModel<String> fileListModel = new DefaultListModel<>();
    private int userId;
    private String currentQuestionType = "简答题"; // 默认题型
    private JPanel columnHeaderPanel;
    private JLabel[] columnLabels;
    
    public class ChoiceOption {
        private String label;
        private String content;

        public ChoiceOption(String label, String content) {
            this.label = label;
            this.content = content;
        }

        // Getters
        public String getLabel() {
            return label;
        }

        public String getContent() {
            return content;
        }
    }


    public UploadPanel(int userId) {
        setLayout(new BorderLayout());
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton directInputButton = new JButton("直接输入题目");
        JButton directImportButton = new JButton("直接导入题目");
        JButton templateImportButton = new JButton("根据模板导入");
        topPanel.add(directInputButton);
        topPanel.add(directImportButton);
        topPanel.add(templateImportButton);
        add(topPanel, BorderLayout.NORTH);

        // 内容面板
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 直接输入题目的面板
        JPanel directInputPanel = createDirectInputPanel();

        // 根据模板导入的面板
        JPanel templateImportPanel = createTemplateImportPanel();
        
        // 直接导入题目的面板
        JPanel directImportPanel = createDirectImportPanel();

        // 添加事件监听器
        directImportButton.addActionListener(e -> cardLayout.show(contentPanel, "DirectImport"));

        contentPanel.add(directInputPanel, "DirectInput");
        contentPanel.add(directImportPanel, "DirectImport");
        contentPanel.add(templateImportPanel, "TemplateImport");
        add(contentPanel, BorderLayout.CENTER);

        // 按钮事件监听器
        directInputButton.addActionListener(e -> cardLayout.show(contentPanel, "DirectInput"));
        templateImportButton.addActionListener(e -> cardLayout.show(contentPanel, "TemplateImport"));
    }
    
    // 直接输入题目
    private JPanel createDirectInputPanel() {
    	JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // 题目类型选择
        JRadioButton essayButton = new JRadioButton("简答题", true); // 默认选中简答题
        JRadioButton multipleChoiceButton = new JRadioButton("选择题");
        ButtonGroup questionTypeGroup = new ButtonGroup();
        questionTypeGroup.add(essayButton);
        questionTypeGroup.add(multipleChoiceButton);
        
        // 添加到面板
        JPanel questionTypePanel = new JPanel(new FlowLayout());
        questionTypePanel.add(essayButton);
        questionTypePanel.add(multipleChoiceButton);
        panel.add(questionTypePanel);

        JTextArea questionContent = new JTextArea(5, 20);
        questionContent.setBorder(BorderFactory.createTitledBorder("题目内容"));
        panel.add(questionContent);
        
        // 答案输入区域
        JPanel answerPanel = new JPanel();
        CardLayout answerLayout = new CardLayout();
        answerPanel.setLayout(answerLayout);
        
        // 选择题答案输入
        JPanel multipleChoicePanel = new JPanel();
        multipleChoicePanel.setLayout(new BoxLayout(multipleChoicePanel, BoxLayout.Y_AXIS));
        HashMap<JRadioButton, JTextField> optionMap = new HashMap<>();
        ButtonGroup choiceGroup = new ButtonGroup();
        char optionLabel = 'A';

        for (int i = 0; i < 4; i++) { // 假设有4个选项
            JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JRadioButton optionButton = new JRadioButton(String.valueOf(optionLabel));
            choiceGroup.add(optionButton);

            JTextField optionTextField = new JTextField(20);
            optionMap.put(optionButton, optionTextField);

            optionPanel.add(optionButton);
            optionPanel.add(optionTextField);

            multipleChoicePanel.add(optionPanel);

            optionLabel++;
        }
        // 简答题答案输入
        JTextArea essayAnswerArea = new JTextArea(3, 20);
        essayAnswerArea.setBorder(BorderFactory.createTitledBorder("答案"));
        
        // 添加到答案面板
        answerPanel.add(multipleChoicePanel, "MultipleChoice");
        answerPanel.add(essayAnswerArea, "Essay");
        
        answerLayout.show(answerPanel, "Essay");
        
        // 添加监听器以切换答案输入区域
        essayButton.addActionListener(e -> answerLayout.show(answerPanel, "Essay"));
        multipleChoiceButton.addActionListener(e -> answerLayout.show(answerPanel, "MultipleChoice"));
 
        panel.add(answerPanel);
        
        JComboBox<String> difficultyLevel = new JComboBox<>(new String[] {"简单", "中等", "困难"});
        difficultyLevel.setBorder(BorderFactory.createTitledBorder("难度级别"));
        panel.add(difficultyLevel);

        JComboBox<String> subjectSelector = new JComboBox<>(new String[] {"语文", "数学", "英语"});
        subjectSelector.setBorder(BorderFactory.createTitledBorder("科目"));
        panel.add(subjectSelector);
        DefaultListModel<String> fileListModel = new DefaultListModel<>();
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 文件上传
        JButton uploadButton = new JButton("上传图片/音频");
        
        
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileFilter(new FileNameExtensionFilter("图片、音频和矢量图文件", "jpg", "png", "wav", "svg"));
                int result = fileChooser.showOpenDialog(panel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] files = fileChooser.getSelectedFiles();
                    for (File file : files) {
                    	ImageIcon icon;
                    	if (file.getName().toLowerCase().endsWith(".svg")) {
                    	    // 对SVG文件进行处理
                    	    icon = new ImageIcon(svgToImage(file));
                    	} else {
                    	    icon = new ImageIcon(file.getAbsolutePath());
                    	}
                        if (icon.getIconWidth() > 600 || icon.getIconHeight() > 600) {
                            JOptionPane.showMessageDialog(panel, "图片尺寸必须在600x600像素以内。", "尺寸错误", JOptionPane.ERROR_MESSAGE);
                        } else {
                            fileListModel.addElement(file.getAbsolutePath());
                        }
                    }
                }
            }
        });
        
                        
        // 提交所有信息
        JButton submitButton = new JButton("提交");
        buttonPanel.add(submitButton);
        buttonPanel.add(uploadButton);
        
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = questionContent.getText().trim();
                String difficulty = (String) difficultyLevel.getSelectedItem();
                String subject = (String) subjectSelector.getSelectedItem();

                String answer = "";
                String questionType = essayButton.isSelected() ? "简答题" : "选择题";

                if (questionType.equals("简答题")) {
                    answer = essayAnswerArea.getText().trim();
                } else {
                	// 选择题
                	boolean allOptionsFilled = true;
                    // 遍历选项，找出被选中的答案
                    for (Map.Entry<JRadioButton, JTextField> entry : optionMap.entrySet()) {
                    	String optionText = entry.getValue().getText().trim();
                    	if (optionText.isEmpty()) {
                            allOptionsFilled = false;
                            break;
                        }
                        if (entry.getKey().isSelected()) {
                            answer = entry.getKey().getText(); // 例如 "A"
                            break;
                        }
                    }
                    if (!allOptionsFilled) {
                        JOptionPane.showMessageDialog(panel, "所有选择题选项都必须填写！", "错误", JOptionPane.ERROR_MESSAGE);
                        return; // 阻止进一步操作
                    }
                }
                
                // 检查题目内容和答案是否为空
                if (content.isEmpty() || answer.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "题目内容和答案都不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                    return; // 阻止进一步操作
                }
                
                // 转换 fileListModel 为 File 列表
                List<File> mediaFiles = new ArrayList<>();
                for (int i = 0; i < fileListModel.size(); i++) {
                    mediaFiles.add(new File(fileListModel.getElementAt(i)));
                }

                // 在这里实现将题目信息保存到数据库的逻辑
                boolean saveSuccessful = saveQuestionToDatabase(userId, content, answer, difficulty, subject, questionType, optionMap, mediaFiles);

                if (saveSuccessful) {
                    JOptionPane.showMessageDialog(UploadPanel.this, "题目上传成功！");
                } else {
                    JOptionPane.showMessageDialog(UploadPanel.this, "上传失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        

        JList<String> fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);
        fileScrollPane.setPreferredSize(new Dimension(350, 20));
        buttonPanel.add(fileScrollPane);
        panel.add(buttonPanel);       
        
        return panel;
    }
    
    private boolean saveQuestionToDatabase(int userId, String questionContent, String answer, String difficulty, String subject, String questionType, HashMap<JRadioButton, JTextField> optionMap, List<File> mediaFiles) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 插入题目到 questions 表
            String insertQuestionSql = "INSERT INTO questions (Subject, Type, Difficulty, Content, Answer, UserID) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, subject);
            stmt.setString(2, questionType);
            stmt.setString(3, difficulty);
            stmt.setString(4, questionContent);
            stmt.setString(5, answer);
            stmt.setInt(6, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating question failed, no rows affected.");
            }

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int questionId = generatedKeys.getInt(1);
                
                // 更新 user_questions 表
                String insertUserQuestionSql = "INSERT INTO user_questions (userID, questionID) VALUES (?, ?)";
                stmt = conn.prepareStatement(insertUserQuestionSql);
                stmt.setInt(1, userId);
                stmt.setInt(2, questionId);
                stmt.executeUpdate();

                if (questionType.equals("选择题")) {
                    // 插入选项到 question_choice 表
                    String insertChoiceSql = "INSERT INTO question_choice (QuestionID, ChoiceLabel, ChoiceContent) VALUES (?, ?, ?)";
                    stmt = conn.prepareStatement(insertChoiceSql);
                        
                    for (int i=0; i<=3; ++i) {
                    	
	                    for (Map.Entry<JRadioButton, JTextField> entry : optionMap.entrySet()) {
	                    	
	                        String choiceLabel = entry.getKey().getText();
	                        String choiceContent = entry.getValue().getText().trim();
	                        if (choiceLabel.equals(String.valueOf((char)('A' + i)))) 
	                        {
		                        stmt.setInt(1, questionId);
		                        stmt.setString(2, choiceLabel);
		                        stmt.setString(3, choiceContent);
		
		                        stmt.addBatch();
	                        }
	                    }
                    }
                    stmt.executeBatch();
                }
                
                // 上传图片或音频文件
                if (!mediaFiles.isEmpty()) {
                    String insertMediaSql = "INSERT INTO questionmedia (QuestionID, MediaType, MediaPath) VALUES (?, ?, ?)";
                    stmt = conn.prepareStatement(insertMediaSql);

                    for (File file : mediaFiles) {
                        String mediaType = file.getName().endsWith(".wav") ? "audio" : "image";
                        stmt.setInt(1, questionId);
                        stmt.setString(2, mediaType);
                        stmt.setString(3, file.getAbsolutePath()); // 或者是上传后的路径

                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }    
            } else {
                throw new SQLException("Creating question failed, no ID obtained.");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    // 直接导入题目
    private JPanel createDirectImportPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 文件选择按钮
        JButton chooseFileButton = new JButton("选择文件");
        JComboBox<String> questionTypeSelector = new JComboBox<>(new String[] {"简答题", "选择题"});
        questionTypeSelector.setSelectedItem("简答题");
        
        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(questionTypeSelector);
        topPanel.add(chooseFileButton);
        panel.add(topPanel, BorderLayout.NORTH);      
        
        // 中间面板，用于放置列名面板和表格
        JPanel middlePanel = new JPanel(new BorderLayout());        
        
        // 设置列名面板               
        if ("选择题".equals(currentQuestionType)) {
	        columnHeaderPanel = new JPanel(new GridLayout(1, 8)); // 选择题最多有8列
	        columnLabels = new JLabel[8];
	        for (int i = 0; i < 8; i++) {
	            columnLabels[i] = new JLabel();
	            columnHeaderPanel.add(columnLabels[i]);
	            middlePanel.add(columnHeaderPanel, BorderLayout.NORTH);
	        }
        }
        else {
        	columnHeaderPanel = new JPanel(new GridLayout(1, 4)); // 简答题最多有4列
            columnLabels = new JLabel[4];
            for (int i = 0; i < 4; i++) {
                columnLabels[i] = new JLabel();
                columnHeaderPanel.add(columnLabels[i]);
                middlePanel.add(columnHeaderPanel, BorderLayout.NORTH);
            }
            
        }
        
        // 初始化表格模型
        DefaultTableModel tableModel = new DefaultTableModel(0, 1);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        middlePanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(middlePanel, BorderLayout.CENTER);        

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 在鼠标释放时更新映射关系和数据模型
                updateColumnToIndexMapping(table);
                updateTableModelColumnOrder(table, tableModel);
            }
        });
        
        // 在选择题目类型的时候更新表格结构
        questionTypeSelector.addActionListener(e -> {
            currentQuestionType = (String) questionTypeSelector.getSelectedItem();
            initializeColumnToIndexMapping(currentQuestionType);
            // 移除旧的列名面板
            middlePanel.remove(columnHeaderPanel);

            // 根据新的题目类型创建新的列名面板
            if ("选择题".equals(currentQuestionType)) {
                columnHeaderPanel = new JPanel(new GridLayout(1, 8));
                columnLabels = new JLabel[8];
                for (int i = 0; i < 8; i++) {
                    columnLabels[i] = new JLabel();
                    columnHeaderPanel.add(columnLabels[i]);
                }
            } else {
                columnHeaderPanel = new JPanel(new GridLayout(1, 4));
                columnLabels = new JLabel[4];
                for (int i = 0; i < 4; i++) {
                    columnLabels[i] = new JLabel();
                    columnHeaderPanel.add(columnLabels[i]);
                }
            }

            // 将新的列名面板添加到中间面板
            middlePanel.add(columnHeaderPanel, BorderLayout.NORTH);
            middlePanel.revalidate();
            middlePanel.repaint();

            // 更新表格结构
            updateTableStructure(tableModel, currentQuestionType, columnLabels);
        });

        chooseFileButton.addActionListener(e -> {
            // 实现文件选择和内容读取
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件", "txt", "csv"));
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                processFile(file, tableModel, currentQuestionType);
            }
        });
        
        // 提交按钮
        JButton submitButton = new JButton("提交");
        submitButton.addActionListener(e -> {
            submitData(table, currentQuestionType);
        });

        // 将提交按钮添加到面板底部
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(submitButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);                
        
        // 初始状态设置为简答题
        updateTableStructure(tableModel, "简答题", columnLabels);
        
        return panel;
    }

    private void updateTableModelColumnOrder(JTable table, DefaultTableModel model) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();

        // 创建数据的临时副本
        Object[][] tempData = new Object[model.getRowCount()][columnCount];
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < columnCount; col++) {
                tempData[row][col] = model.getValueAt(row, col);
            }
        }

        // 更新数据模型
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < columnCount; col++) {
                TableColumn tableColumn = columnModel.getColumn(col);
                int modelIndex = columnToIndexMapping.get(tableColumn.getHeaderValue().toString());
                model.setValueAt(tempData[row][modelIndex], row, col);
            }
        }
    }

    // 更新表格结构的方法
    private void updateTableStructure(DefaultTableModel model, String questionType, JLabel[] columnLabels) {
        model.setRowCount(0); // 清空现有数据
        model.setColumnCount(0); // 清空现有列

        if ("简答题".equals(questionType)) {
            String[] headers = new String[]{"题目内容", "答案", "难度", "科目"};
            for (int i = 0; i < headers.length; i++) {
                columnLabels[i].setText(headers[i]);
                columnLabels[i].setVisible(true);
            }
            for (int i = headers.length; i < columnLabels.length; i++) {
                columnLabels[i].setVisible(false); // 隐藏多余的列名标签
            }
            model.setColumnCount(4); // 简答题有4列数据
        } else if ("选择题".equals(questionType)) {
            String[] headers = new String[]{"题目内容", "选项A", "选项B", "选项C", "选项D", "正确答案", "难度", "科目"};
            for (int i = 0; i < headers.length; i++) {
                columnLabels[i].setText(headers[i]);
                columnLabels[i].setVisible(true);
            }
            model.setColumnCount(8); // 选择题有8列数据
        }
    }

    
    // 读取文件并填充表格
    private void processFile(File file, DefaultTableModel model, String questionType) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (("简答题".equals(questionType) && values.length == 4) ||
                    ("选择题".equals(questionType) && values.length == 8)) {
                    model.addRow(values);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "文件读取错误！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 在类内部定义映射关系
    private Map<String, Integer> columnToIndexMapping;

    // 初始化映射关系
    private void initializeColumnToIndexMapping(String questionType) {
        columnToIndexMapping = new HashMap<>();
        if ("简答题".equals(questionType)) {
            columnToIndexMapping.put("题目内容", 0);
            columnToIndexMapping.put("答案", 1);
            columnToIndexMapping.put("难度", 2);
            columnToIndexMapping.put("科目", 3);
        } else if ("选择题".equals(questionType)) {
            columnToIndexMapping.put("题目内容", 0);
            columnToIndexMapping.put("选项A", 1);
            columnToIndexMapping.put("选项B", 2);
            columnToIndexMapping.put("选项C", 3);
            columnToIndexMapping.put("选项D", 4);
            columnToIndexMapping.put("正确答案", 5);
            columnToIndexMapping.put("难度", 6);
            columnToIndexMapping.put("科目", 7);
        }
    }
    // 更新映射关系的方法
    private void updateColumnToIndexMapping(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        columnToIndexMapping.clear(); // 清除旧的映射
        for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
        	TableColumn tableColumn = columnModel.getColumn(columnIndex);
            String columnName = tableColumn.getHeaderValue().toString();
            columnToIndexMapping.put(columnName, tableColumn.getModelIndex());
        }
    }
    
    // 提交数据到数据库
    private void submitData(JTable table, String questionType) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // 更新映射关系
        updateColumnToIndexMapping(table);

        // 提交每行数据
        for (int row = 0; row < model.getRowCount(); row++) {
            String[] rowData = new String[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                String columnName = table.getColumnName(col);
                Integer modelIndex = columnToIndexMapping.get(columnName);
                if (modelIndex != null) {
                    rowData[col] = (String) model.getValueAt(row, modelIndex);
                } else {
                    rowData[col] = ""; // 或其他适当的默认值
                }
            }
            
         // 根据重组后的数据行进行数据库提交
            if ("简答题".equals(questionType)) {
                saveEssayQuestionToDatabase(userId, rowData[0], rowData[1], rowData[2], rowData[3]);
            } else if ("选择题".equals(questionType)) {
                List<String> choices = Arrays.asList(rowData[1], rowData[2], rowData[3], rowData[4]);
                saveMultipleChoiceQuestionToDatabase(userId, rowData[0], choices, rowData[5], rowData[6], rowData[7]);
            }
        }

        JOptionPane.showMessageDialog(null, "提交成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    
    private boolean saveEssayQuestionToDatabase(int userId, String content, String answer, String difficulty, String subject) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 插入题目到 questions 表
            String sql1 = "INSERT INTO questions (UserID, Subject, Type, Difficulty, Content, Answer) VALUES (?, ?, '简答题', ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setInt(1, userId);
                stmt.setString(2, subject);
                stmt.setString(3, difficulty);
                stmt.setString(4, content);
                stmt.setString(5, answer);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveMultipleChoiceQuestionToDatabase(int userId, String content, List<String> choices, String correctAnswer, String difficulty, String subject) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 插入题目到 questions 表
            String insertQuestionSql = "INSERT INTO questions (UserID, Subject, Type, Difficulty, Content, Answer) VALUES (?, ?, '选择题', ?, ?, ?)";
            stmt = conn.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, subject);
            stmt.setString(3, difficulty);
            stmt.setString(4, content);
            stmt.setString(5, correctAnswer);
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int questionId = generatedKeys.getInt(1);

                // 插入选项到 question_choice 表
                String insertChoiceSql = "INSERT INTO question_choice (QuestionID, ChoiceLabel, ChoiceContent) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(insertChoiceSql);

                char label = 'A';
                for (String choice : choices) {
                    stmt.setInt(1, questionId);
                    stmt.setString(2, String.valueOf(label));
                    stmt.setString(3, choice);
                    stmt.addBatch();
                    label++;
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 根据模板导入
    private JPanel createTemplateImportPanel() {
    	JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // 文件选择按钮
        JButton chooseFileButton = new JButton("选择文件");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件", "csv"));
        panel.add(chooseFileButton);
        
        chooseFileButton.addActionListener(e -> {
            int returnVal = fileChooser.showOpenDialog(panel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File file = fileChooser.getSelectedFile();
                fileListModel.addElement(file.getAbsolutePath()); // 添加文件路径到列表
            }
        });

        JLabel formatLabel = new JLabel("支持的格式: .csv");
        panel.add(formatLabel);

        JButton importButton = new JButton("导入");
        panel.add(importButton);        

        importButton.addActionListener(e -> {
            if (!fileListModel.isEmpty()) {
                // 从列表中获取已选择的文件并进行导入
                for (int i = 0; i < fileListModel.size(); i++) {
                    String filePath = fileListModel.getElementAt(i);
                    File file = new File(filePath);
                    importQuestionsFromCsv(file);
                }
                JOptionPane.showMessageDialog(UploadPanel.this, "模板上传成功！");
                fileListModel.clear(); // 清空文件列表
            } else {
                JOptionPane.showMessageDialog(panel, "请先选择文件！", "警告", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 文件列表显示
        JList<String> fileList = new JList<>(fileListModel);
        fileList.setVisibleRowCount(2);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane fileScrollPane = new JScrollPane(fileList);
        panel.add(fileScrollPane);
        
        // 格式说明
        JLabel fileFormatLabel = new JLabel("<html><br/>" +
        		"<br/>" +
        		"<br/>" +
        		"具体格式说明：<br/>" +
        		"<br/>" +
                "- 简答题：题目内容,答案,难度,科目,音频/图像地址<br/>" +
                "- 选择题：题目内容,选项A,选项B,选项C,选项D,正确答案,难度,科目,音频/图像地址</html>");
        fileFormatLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        panel.add(fileFormatLabel);

        return panel;
    }    
    
    private void importQuestionsFromCsv(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                try {
                	String mediaPath = (values.length > 4) ? values[values.length - 1].trim() : null; // 最后一列是媒体路径
                	if (values.length == 5 || values.length == 9) { // 假设现在简答题有5列，选择题有9列
                        int questionId = (values.length == 5) ? processEssayQuestion(values) : processMultipleChoiceQuestion(values);
                        if (questionId != -1 && mediaPath != null && !mediaPath.isEmpty()) {
                            saveMediaToDatabase(questionId, mediaPath);
                        }
                    } else {
                        // 格式不匹配
                        System.out.println("Skipped line due to incorrect format: " + line);
                    }

                } catch (Exception e) {
                    System.out.println("Error processing line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
            JOptionPane.showMessageDialog(null, "文件读取错误！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveMediaToDatabase(int questionId, String mediaPath) {
    	String mediaType = mediaPath.endsWith(".wav") ? "audio" : "image";
        String sql = "INSERT INTO questionmedia (QuestionID, MediaType, MediaPath) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.setString(2, mediaType);
            stmt.setString(3, mediaPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private int processEssayQuestion(String[] values) {
        String content = values[0].trim();
        String answer = values[1].trim();
        String difficulty = values[2].trim();
        String subject = values[3].trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO questions (UserID, Subject, Type, Difficulty, Content, Answer) VALUES (?, ?, '简答题', ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setString(2, subject);
                stmt.setString(3, difficulty);
                stmt.setString(4, content);
                stmt.setString(5, answer);
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                    	int questionId = generatedKeys.getInt(1);
                    	updateUserQuestionsTable(userId, questionId); // 更新 user_questions 表
                        return questionId; // 返回生成的题目ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 如果保存失败
    }
    
    private int processMultipleChoiceQuestion(String[] values) {
        String content = values[0].trim();
        List<String> choices = Arrays.asList(Arrays.copyOfRange(values, 1, 5));
        String correctAnswer = values[5].trim();
        String difficulty = values[6].trim();
        String subject = values[7].trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String insertQuestionSql = "INSERT INTO questions (UserID, Subject, Type, Difficulty, Content, Answer) VALUES (?, ?, '选择题', ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setString(2, subject);
                stmt.setString(3, difficulty);
                stmt.setString(4, content);
                stmt.setString(5, correctAnswer);
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                    	int questionId = generatedKeys.getInt(1);
                        updateUserQuestionsTable(userId, questionId); // 更新 user_questions 表

                        String insertChoiceSql = "INSERT INTO question_choice (QuestionID, ChoiceLabel, ChoiceContent) VALUES (?, ?, ?)";
                        try (PreparedStatement choiceStmt = conn.prepareStatement(insertChoiceSql)) {
                            char label = 'A';
                            for (String choice : choices) {
                                choiceStmt.setInt(1, questionId);
                                choiceStmt.setString(2, String.valueOf(label));
                                choiceStmt.setString(3, choice);
                                choiceStmt.addBatch();
                                label++;
                            }
                            choiceStmt.executeBatch();
                        }

                        return questionId; // 返回生成的题目ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 如果保存失败
    }
    
    private void updateUserQuestionsTable(int userId, int questionId) {
        String sql = "INSERT INTO user_questions (userID, questionID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, questionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Image svgToImage(File svgFile) {
        try {
            String svgURI = svgFile.toURI().toString();
            TranscoderInput input = new TranscoderInput(svgURI);
            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
            transcoder.transcode(input, null);
            return transcoder.getBufferedImage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // BufferedImageTranscoder 类
    class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image = null;

        public BufferedImage getBufferedImage() {
            return image;
        }

        @Override
        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput out) {
            image = img;
        }
    }

    
}

