package com.java.gui.panels;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.java.database.DatabaseConnection;
import com.java.gui.main.MainFrame;
import com.java.models.Question;
import com.java.models.QuestionMedia;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowsePanel extends JPanel {
    private JComboBox<String> subjectComboBox;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> difficultyComboBox;
	private JPanel resultPanel;
	private JTextField searchField;
	
	private MainFrame mainFrame;
    private int userId;
	
	private static final int QUESTIONS_PER_PAGE = 8;
    private List<Question> allQuestions; // 存储所有题目
    private int currentPage = 0; // 当前页码，从0开始
    private Map<Integer, Boolean> questionSelectionMap = new HashMap<>();
    
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            displayAllQuestions();
        }
    }
    
    public BrowsePanel(MainFrame mainFrame, int userId) {
        this.mainFrame = mainFrame;
        this.userId = userId;
        
        setBackground(new Color(240, 240, 240));
        setLayout(new BorderLayout(10, 10));
        
        // 检索框和搜索、重置按钮
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(25);
        JButton searchButton = new JButton("搜索");
        
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 执行搜索逻辑
                searchQuestions(searchField.getText());
            }
        });
        JButton resetButton = new JButton("重置");
        
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFilters();
            }
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        // 查找条件设置面板
        JPanel filterPanel = new JPanel();
        subjectComboBox = new JComboBox<>(new String[] {"默认", "语文", "数学", "英语"});
        typeComboBox = new JComboBox<>(new String[] {"默认", "选择题", "简答题"});
        difficultyComboBox = new JComboBox<>(new String[] {"默认", "简单", "中等", "困难"});
        
        filterPanel.add(new JLabel("科目:"));
        filterPanel.add(subjectComboBox);
        filterPanel.add(new JLabel("题型:"));
        filterPanel.add(typeComboBox);
        filterPanel.add(new JLabel("难度:"));
        filterPanel.add(difficultyComboBox);
        
        // 创建顶部面板并添加检索和过滤器面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        // 结果显示区域
        resultPanel = new JPanel();
        resultPanel.setBackground(new Color(255, 255, 255));
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        allQuestions = new ArrayList<>();
        displayAllQuestions();

        // 添加组件到BrowsePanel
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // 创建翻页按钮
        JButton prevButton = new JButton("上一页");
        JButton nextButton = new JButton("下一页");
        JPanel pageControlPanel = new JPanel();
        pageControlPanel.add(prevButton);
        pageControlPanel.add(nextButton);

        add(pageControlPanel, BorderLayout.SOUTH);

        // 添加按钮事件监听器
        prevButton.addActionListener(e -> changePage(false));
        nextButton.addActionListener(e -> changePage(true));
        
        // 添加批量导出按钮
        JButton exportButton = new JButton("批量导出");
        pageControlPanel.add(exportButton);

        // 添加事件监听器
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportSelectedQuestions();
            }
        });

    }
    
    private void resetFilters() {
        // 清空搜索框并重置下拉框选项
        searchField.setText("");
        subjectComboBox.setSelectedItem("默认");
        typeComboBox.setSelectedItem("默认");
        difficultyComboBox.setSelectedItem("默认");
    }
    
    private void changePage(boolean next) {
        if (next && (currentPage + 1) * QUESTIONS_PER_PAGE < allQuestions.size()) {
            currentPage++;
        } else if (!next && currentPage > 0) {
            currentPage--;
        }
        updateResultPanel();
    }
    
    private void displayAllQuestions() {
        // 从数据库获取题目
        allQuestions = getAllQuestionsFromDatabase();
        updateResultPanel();
    }

    private List<Question> getAllQuestionsFromDatabase() {
        // 连接数据库并获取所有题目
        List<Question> questions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement()) {
            String sql = "SELECT * FROM Questions";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
            	questions.add(new Question(rs.getInt("QuestionID"),rs.getString("Subject"), rs.getString("Type"), rs.getString("Difficulty"), rs.getString("Content"), rs.getString("Answer")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private JPanel createQuestionCard(Question question) {
    	int questionId = question.getId();
    	JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // 设置卡片的固定尺寸
        int cardWidth = 200;
        int cardHeight = 100;
        card.setPreferredSize(new Dimension(cardWidth, cardHeight));
        card.setMaximumSize(new Dimension(cardWidth, cardHeight));
        
        // 在左上角显示科目和题型
        String subject = question.getSubject();
        String type = question.getType();
        JLabel subjectTypeLabel = new JLabel(subject + " - " + type);
        subjectTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(subjectTypeLabel, BorderLayout.NORTH);

        // 题目内容居中显示
        String content = question.getContent();
        JLabel contentLabel = new JLabel("<html><center>" + content + "</center></html>");
    	java.awt.Font awtFont = new java.awt.Font("宋体", java.awt.Font.BOLD, 20);
    	contentLabel.setFont(awtFont);
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(contentLabel, BorderLayout.CENTER);
        
        // 添加鼠标点击事件
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 创建并显示题目详情面板
                // 获取题目详细信息
                Question detailedQuestion = getQuestionFromDatabase(questionId);
                mainFrame.showQuestionDetail(detailedQuestion, userId);
            }
        });
        
        // 添加复选框
        JCheckBox checkBox = new JCheckBox();
        checkBox.setActionCommand(String.valueOf(question.getId())); // 使用题目ID作为命令字符串
        
        // 设置复选框的选中状态
        checkBox.setSelected(questionSelectionMap.getOrDefault(question.getId(), false));

        // 添加复选框状态改变的监听器
        checkBox.addItemListener(e -> questionSelectionMap.put(question.getId(), checkBox.isSelected()));
        card.add(checkBox, BorderLayout.SOUTH);

        return card;
    }
    
    public void performSearch(String searchQuery) {
        // 将搜索字符串设置到搜索框中
    	searchField.setText(searchQuery); // 设置搜索框的值
        // 执行搜索逻辑
        searchQuestions(searchQuery);
    }

    private void searchQuestions(String query) {
    	
    	// 清空 allQuestions 列表
        allQuestions.clear();
        currentPage = 0; // 重置到第一页
    	
    	List<Question> questions = new ArrayList<>();
    	StringBuilder sql = new StringBuilder("SELECT * FROM Questions WHERE Content LIKE ?");
        List<String> params = new ArrayList<>();
        
        // 添加检索词到参数列表
        params.add("%" + query + "%");
        
     	// 检查并添加科目条件
        if (!"默认".equals(subjectComboBox.getSelectedItem().toString())) {
            sql.append(" AND Subject = ?");
            params.add(subjectComboBox.getSelectedItem().toString());
        }

        // 检查并添加题型条件
        if (!"默认".equals(typeComboBox.getSelectedItem().toString())) {
            sql.append(" AND Type = ?");
            params.add(typeComboBox.getSelectedItem().toString());
        }

        // 检查并添加难度条件
        if (!"默认".equals(difficultyComboBox.getSelectedItem().toString())) {
            sql.append(" AND Difficulty = ?");
            params.add(difficultyComboBox.getSelectedItem().toString());
        }
        
        // 连接数据库并执行查询
        try (Connection connection = DatabaseConnection.getConnection();
        	PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            // 设置查询参数
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            // 执行查询
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                	questions.add(new Question(rs.getInt("QuestionID"),rs.getString("Subject"), rs.getString("Type"), rs.getString("Difficulty"), rs.getString("Content"), rs.getString("Answer")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 更新 allQuestions 列表并重置当前页码
        allQuestions = questions;
        currentPage = 0;

        // 更新结果面板
        updateResultPanel();
    }
    
    private void updateResultPanel() {
        resultPanel.removeAll();
        resultPanel.setLayout(new GridLayout(0, 2, 10, 10)); // 第二个参数为2表示两列
        int start = currentPage * QUESTIONS_PER_PAGE;
        int end = Math.min(start + QUESTIONS_PER_PAGE, allQuestions.size());
        for (int i = start; i < end; i++) {
            Question question = allQuestions.get(i);
            JPanel card = createQuestionCard(question);
            resultPanel.add(card);
        }
        // 如果当前页的卡片数量少于 QUESTIONS_PER_PAGE，则添加空白卡片以保持布局一致
        for (int i = end; i < start + QUESTIONS_PER_PAGE; i++) {
            JPanel emptyCard = new JPanel();
            emptyCard.setPreferredSize(new Dimension(200, 100)); // 使用与其他卡片相同的尺寸
            emptyCard.setOpaque(false);
            resultPanel.add(emptyCard);
        }
        
        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    private Question getQuestionFromDatabase(int questionId) {
    	Question question = null;
        String sql = "SELECT * FROM questions WHERE QuestionID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("QuestionID");
                String subject = rs.getString("Subject");
                String type = rs.getString("Type");
                String difficulty = rs.getString("Difficulty");
                String content = rs.getString("Content");
                String answer = rs.getString("Answer");

                question = new Question(id, subject, type, difficulty, content, answer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return question;
    }
    
    private void exportSelectedQuestions() {
        List<Question> selectedQuestions = new ArrayList<>();
        for (Component comp : resultPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                for (Component c : card.getComponents()) {
                    if (c instanceof JCheckBox) {
                        JCheckBox checkBox = (JCheckBox) c;
                        if (checkBox.isSelected()) {
                            int questionId = Integer.parseInt(checkBox.getActionCommand());
                            Question q = getQuestionFromDatabase(questionId);
                            if (q != null) {
                                selectedQuestions.add(q);
                            }
                        }
                    }
                }
            }
        }

        if (!selectedQuestions.isEmpty()) {
            // 调用导出逻辑
            exportQuestionsToPdf(selectedQuestions);
        } else {
            JOptionPane.showMessageDialog(this, "未选择任何题目");
        }
    }
    
    private void exportQuestionsToPdf(List<Question> questions) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存 PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }

            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();
                String fontPath = "src/font/simsun.ttf";

                for (Question question : questions) {
                    try {
    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	        	    Font font = new Font(base, 15);
    		        	Paragraph paragraph = new Paragraph("题目: ", font);
    		        	document.add(paragraph);
                    }catch (DocumentException e) {
    	        	    e.printStackTrace();
    	        	} catch (IOException e) {
    	        	    // 处理字体文件加载失败的情况
    	        	    e.printStackTrace();
    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	        	}
                    
                    try {
    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	        	    Font font = new Font(base, 15);
    		        	Paragraph paragraph = new Paragraph(question.getContent(),font);
    		        	document.add(paragraph);
    		        
    	        	} catch (DocumentException e) {
    	        	    e.printStackTrace();
    	        	} catch (IOException e) {
    	        	    // 处理字体文件加载失败的情况
    	        	    e.printStackTrace();
    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	        	}
    	        	
    	        	
    	            // 如果是选择题，添加选项
    	            if (question.getType().equals("选择题")) {
    	                List<String> options = getOptionsFromDatabase(question.getId());
    	                for (String option : options) {
    	                	try {
    	    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	    	        	    Font font = new Font(base, 15);
    	    		        	Paragraph paragraph = new Paragraph((option),font);
    	    		        	document.add(paragraph);
    	    		        
    	    	        	} catch (DocumentException e) {
    	    	        	    e.printStackTrace();
    	    	        	} catch (IOException e) {
    	    	        	    // 处理字体文件加载失败的情况
    	    	        	    e.printStackTrace();
    	    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	    	        	}
    	                }
    	            }	            
    	            
    	            // 添加图片（如果有）
    	            List<QuestionMedia> mediaList = getMediaFromDatabase(question.getId());
    	            for (QuestionMedia media : mediaList) {
    	                if ((media.getMediaType().equals("image")) && !(media.getMediaPath().equals("null"))) {
    	                	if (media.getMediaPath().endsWith(".svg")) {
    	                        try {
    	                            String svgURI = new File(media.getMediaPath()).toURI().toString();
    	                            TranscoderInput input_svg_image = new TranscoderInput(svgURI);                
    	                            ByteArrayOutputStream svg_output_stream = new ByteArrayOutputStream();
    	                            TranscoderOutput output_png_image = new TranscoderOutput(svg_output_stream);                
    	                            PNGTranscoder my_converter = new PNGTranscoder();
    	                            my_converter.transcode(input_svg_image, output_png_image);
    	                            svg_output_stream.flush();

    	                            // 将转换后的SVG添加到PDF
    	                            Image image = Image.getInstance(svg_output_stream.toByteArray());
    	                            document.add(image);
    	                        } catch (Exception e) {
    	                            e.printStackTrace();
    	                        }
    	                	} else {
    		                	try {
    		    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    		    	        	    Font font = new Font(base, 15);
    		    		        	Paragraph paragraph = new Paragraph("      ", font);
    		    		        	document.add(paragraph);
    		    	        	} catch (DocumentException e) {
    		    	        	    e.printStackTrace();
    		    	        	} catch (IOException e) {
    		    	        	    // 处理字体文件加载失败的情况
    		    	        	    e.printStackTrace();
    		    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    		    	        	}
    		                	try {
    		                        Image image = Image.getInstance(media.getMediaPath());
    		                        document.add(image);
    		                    } catch (MalformedURLException e) {
    		                        e.printStackTrace(); // 处理 URL 格式异常
    		                    } catch (IOException e) {
    		                        e.printStackTrace(); // 处理 IO 异常
    		                    }
    	                	}
    	                } else if (media.getMediaType().equals("audio")) {
    	                	try {
    	    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	    	        	    Font font = new Font(base, 15);
    	    		        	Paragraph paragraph = new Paragraph("注意: 此题目包含音频内容，请在导出音频后查看。 ", font);
    	    		        	document.add(paragraph);
    	    	        	} catch (DocumentException e) {
    	    	        	    e.printStackTrace();
    	    	        	} catch (IOException e) {
    	    	        	    // 处理字体文件加载失败的情况
    	    	        	    e.printStackTrace();
    	    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	    	        	}
    	                } 
    	            }
    	            
    	            // 添加答案
    	        	try {
    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	        	    Font font = new Font(base, 15);
    		        	Paragraph paragraph = new Paragraph("答案: ", font);
    		        	document.add(paragraph);
    	        	} catch (DocumentException e) {
    	        	    e.printStackTrace();
    	        	} catch (IOException e) {
    	        	    // 处理字体文件加载失败的情况
    	        	    e.printStackTrace();
    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	        	}
    	        	
    	        	try {
    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	        	    Font font = new Font(base, 15);
    		        	Paragraph paragraph = new Paragraph(question.getAnswer(), font);
    		        	document.add(paragraph);
    	        	} catch (DocumentException e) {
    	        	    e.printStackTrace();
    	        	} catch (IOException e) {
    	        	    // 处理字体文件加载失败的情况
    	        	    e.printStackTrace();
    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	        	}
    	        	
    	        	try {
    	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	        	    Font font = new Font(base, 15);
    		        	Paragraph paragraph = new Paragraph("    ", font);
    		        	document.add(paragraph);
                    }catch (DocumentException e) {
    	        	    e.printStackTrace();
    	        	} catch (IOException e) {
    	        	    // 处理字体文件加载失败的情况
    	        	    e.printStackTrace();
    	        	    JOptionPane.showMessageDialog(null, "字体文件加载失败，请检查路径: " + fontPath, "错误", JOptionPane.ERROR_MESSAGE);
    	        	}
                }
    	            
    	        document.close();
    	        JOptionPane.showMessageDialog(this, "PDF 文件已导出到: " + filePath);
    	    } catch (DocumentException | FileNotFoundException e) {
    	        e.printStackTrace();
    	        JOptionPane.showMessageDialog(this, "导出 PDF 时发生错误");
    	    }
        }
           
    }
    
    private List<String> getOptionsFromDatabase(int questionId) {
        List<String> options = new ArrayList<>();
        // 实现从数据库获取选择题选项的逻辑
        String sql = "SELECT ChoiceLabel, ChoiceContent FROM question_choice WHERE QuestionID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String label = rs.getString("ChoiceLabel");
                String content = rs.getString("ChoiceContent");
                options.add(label + ": " + content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }
    
    private List<QuestionMedia> getMediaFromDatabase(int questionId) {
        List<QuestionMedia> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM QuestionMedia WHERE QuestionID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int mediaID = rs.getInt("MediaID");
                    String mediaType = rs.getString("MediaType");
                    String mediaPath = rs.getString("MediaPath");

                    mediaList.add(new QuestionMedia(mediaID, questionId, mediaType, mediaPath));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mediaList;
    }
    



}


