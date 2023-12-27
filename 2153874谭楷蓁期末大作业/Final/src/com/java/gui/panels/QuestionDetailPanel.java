package com.java.gui.panels;

import com.java.database.DatabaseConnection;
import com.java.gui.main.MainFrame;
import com.java.models.Question;
import com.java.models.QuestionMedia;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
//import com.itextpdf.text.pdf.parser.clipper.Paths;
import com.itextpdf.text.Image;
import com.itextpdf.text.Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.Timer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class QuestionDetailPanel extends JPanel {
	private MainFrame mainFrame;
    private Question question;
    private JLabel answerLabel;
    private JButton showAnswerButton;
    private JButton collectButton;
    private int userId; // 用户ID，用于收藏功能
    private JButton exportPdfButton; // 导出pdf按钮
    private JButton exportAudioButton; // 导出音频按钮
    private JButton backButton; // 返回按钮
    
    private Clip audioClip; // 音频剪辑
    private JButton playPauseButton; // 播放/暂停按钮
    private JSlider audioProgressSlider; // 音频进度条
    private Timer updateTimer; // 更新进度条的计时器

    public QuestionDetailPanel(Question question, int userId, MainFrame mainFrame) {
    	this.mainFrame = mainFrame;
    	this.question = question;
        this.userId = userId;
        initializeUI();
    }

    private void initializeUI() {
    	setLayout(new BorderLayout());
    	
        // 题目内容
    	JPanel contentPanel = new JPanel();
    	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    	// 题目内容使用 JTextArea
    	JTextArea contentLabel = new JTextArea(question.getContent());
    	java.awt.Font awtFont = new java.awt.Font("宋体", java.awt.Font.PLAIN, 22);
    	contentLabel.setFont(awtFont);
    	contentLabel.setWrapStyleWord(true);
    	contentLabel.setLineWrap(true);
    	contentLabel.setEditable(false);
    	contentLabel.setBackground(null); // 设置背景为透明
    	contentLabel.setBorder(null); // 去除边框
    	contentPanel.add(contentLabel);

        // 如果是选择题，显示选项
        if (question.getType().equals("选择题")) {
            displayOptions(contentPanel);
        }
        
        // 添加媒体内容展示
        JPanel mediaPanel = new JPanel();
        displayMediaContent(mediaPanel);
        
        // 将内容面板添加到滚动面板
        JPanel APanel = new JPanel();
        APanel.setLayout(new BoxLayout(APanel, BoxLayout.Y_AXIS));
        APanel.add(contentPanel);
        APanel.add(mediaPanel);
        JScrollPane scrollPane = new JScrollPane(APanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加边距
        add(scrollPane, BorderLayout.CENTER);
        
        // 操作按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // 设置间距

        // 答案（初始时隐藏）
        answerLabel = new JLabel("答案: " + question.getAnswer());
        answerLabel.setVisible(false); // 初始不可见
        buttonPanel.add(answerLabel);

        // 显示答案按钮
        showAnswerButton = new JButton("显示答案");
        showAnswerButton.addActionListener(e -> answerLabel.setVisible(true));
        buttonPanel.add(showAnswerButton);

        // 收藏按钮
        collectButton = new JButton("收藏");
        collectButton.addActionListener(e -> collectQuestion());
        buttonPanel.add(collectButton);
        
        // 导出到 PDF 按钮
        exportPdfButton = new JButton("导出到 PDF");
        exportPdfButton.addActionListener(e -> exportQuestionToPdf());
        buttonPanel.add(exportPdfButton);
        
        // 检查是否有音频文件
        List<QuestionMedia> mediaList = getMediaFromDatabase(question.getId());
        for (QuestionMedia media : mediaList) {
            if (media.getMediaType().equals("audio")) {
                exportAudioButton = new JButton("导出音频");
                exportAudioButton.addActionListener(e -> exportAudioFile(media.getMediaPath()));
                buttonPanel.add(exportAudioButton); // 添加到按钮面板
            }
        }
        
        // 返回按钮
        backButton = new JButton("返回");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.BackToBrowsePanel();
            }
        });
        buttonPanel.add(backButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
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
    
    private void displayMediaContent(JPanel mediaPanel) {
        // 从数据库获取媒体文件信息
        List<QuestionMedia> mediaList = getMediaFromDatabase(question.getId());
        for (QuestionMedia media : mediaList) {
            switch (media.getMediaType()) {
                case "image":
                	if (media.getMediaPath().toLowerCase().endsWith(".svg")) {
                        // SVG图片处理
                        try {
                            String svgURI = new File(media.getMediaPath()).toURI().toURL().toString();
                            TranscoderInput input = new TranscoderInput(svgURI);
                            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
                            transcoder.transcode(input, null);
                            BufferedImage bufferedImage = transcoder.getBufferedImage();
                            ImageIcon icon = new ImageIcon(bufferedImage);
                            mediaPanel.add(new JLabel(icon));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 普通图片处理
                        mediaPanel.add(new JLabel(new ImageIcon(media.getMediaPath())));
                    }
                    break;
                case "audio":
                	JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    
                	audioClip = loadAudioClip(media.getMediaPath());
                    audioProgressSlider = new JSlider();
                    audioProgressSlider.setValue(0);
                    playPauseButton = new JButton("播放");

                    playPauseButton.addActionListener(e -> toggleAudioPlayback(audioClip, playPauseButton));
                    audioProgressSlider.addChangeListener(e -> adjustAudioPosition());

                    audioPanel.add(audioProgressSlider);
                    audioPanel.add(playPauseButton);
                    
                    mediaPanel.add(audioPanel);
                    
                    updateTimer = new Timer(100, e -> updateAudioProgress());
                    break;
            }
        }
    }
    
    // 添加转换器类
    class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.image = img;
        }

        public BufferedImage getBufferedImage() {
            return image;
        }
    }
    
    // 获取音频Clip
    private Clip loadAudioClip(String filePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            Clip audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            return audioClip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // 切换音频播放和暂停
    private void toggleAudioPlayback(Clip audioClip, JButton playButton) {
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                audioClip.stop();
                playPauseButton.setText("播放");
                updateTimer.stop();
            } else {
            	audioClip.setFramePosition(audioProgressSlider.getValue() * audioClip.getFrameLength() / 100);
                audioClip.start();
                playPauseButton.setText("暂停");
                updateTimer.start();
            }
        }
    }

    // 更新音频进度条
    private void updateAudioProgress() {
        if (audioClip != null && audioClip.isRunning()) {
            int position = audioClip.getFramePosition();
            int total = audioClip.getFrameLength();
            int progress = (int) (((double) position / total) * 100);
            audioProgressSlider.setValue(progress);
        }
    }
    // 调整进度条位置
    private void adjustAudioPosition() {
        if (!audioClip.isRunning()) {
            int newPosition = audioProgressSlider.getValue() * audioClip.getFrameLength() / 100;
            audioClip.setFramePosition(newPosition);
        }
    }

    private void displayOptions(JPanel contentPanel) {
        // 从数据库获取选择题的选项，并显示
        //optionsPanel = new JPanel();
        //optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        List<String> options = getOptionsFromDatabase(question.getId());
        for (String option : options) {
        	JTextArea optionLabel = new JTextArea(option);
        	java.awt.Font awtFont = new java.awt.Font("宋体", java.awt.Font.PLAIN, 16);
        	optionLabel.setFont(awtFont);
        	optionLabel.setWrapStyleWord(true);
        	optionLabel.setLineWrap(true);
        	optionLabel.setEditable(false);
        	optionLabel.setBackground(null); // 设置背景为透明
        	optionLabel.setBorder(null); // 去除边框
        	contentPanel.add(optionLabel);
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

    private void collectQuestion() {
    	// 首先检查是否已收藏
    	if (isQuestionAlreadyCollected()) {
            JOptionPane.showMessageDialog(this, "该题目已被收藏");
            return;
        }
        // 实现收藏题目的逻辑
        String sql = "INSERT INTO favorites (UserID, QuestionID) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, question.getId());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "收藏成功！");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void exportQuestionToPdf() {
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
	        	PdfWriter.getInstance(document, new FileOutputStream(filePath)); // 使用用户选择的路径
	        	document.open();
	        	// 定义中文字体的路径
	        	String fontPath = "src/font/simsun.ttf";
	        	
	        	try {
	        	    BaseFont base = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	        	    Font font = new Font(base, 15);
		        	Paragraph paragraph = new Paragraph("题目: ", font);
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
	            
	            document.close();
	            JOptionPane.showMessageDialog(this, "PDF 文件已导出到: " + filePath);
	        } catch (DocumentException | FileNotFoundException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "导出 PDF 时发生错误");
	        }
        }
    }
    
    // 实现导出音频文件的方法
    private void exportAudioFile(String audioFilePath) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存音频文件");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Files.copy(Paths.get(audioFilePath), Paths.get(selectedFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "音频文件已导出到: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "导出音频时发生错误");
            }
        }
    }
    
    private boolean isQuestionAlreadyCollected() {
        String checkSql = "SELECT * FROM favorites WHERE UserID = ? AND QuestionID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, question.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return true;  // 已存在收藏
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // 未收藏
    }
    
}


