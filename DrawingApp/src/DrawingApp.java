import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.awt.Dimension;
import java.awt.Font;

public class DrawingApp extends JFrame {
    private JButton pointButton, lineButton, arcButton, circleButton, rectangleButton, keyboardDrawingButton;
    private JComboBox<String> lineStyleComboBox;
    private JSlider lineWidthSlider;
    private JButton colorButton;
    private Color selectedColor;
    private List<Shape> shapes;
    private Shape currentShape;
    private JButton saveButton, openButton;
    private BufferedImage canvasImage; // 新增变量，用于存储和操作图像
    private JPanel drawingPanel;
    private boolean showCoordinates = false;

    public DrawingApp() {
        setTitle("绘图应用");
        setLayout(new BorderLayout());
        setSize(1800,1400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 设置字体
        Font largeFont = new Font("Serif", Font.BOLD, 30); // 创建一个新的字体对象

		JPanel filePanel = new JPanel(); // 用于保存和打开文件的按钮
        JPanel shapePanel = new JPanel(); // 用于绘制形状的按钮
        JPanel optionsPanel = new JPanel(); // 用于选项，如线型和线宽

        filePanel.setLayout(new FlowLayout());
        shapePanel.setLayout(new FlowLayout());
        optionsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        pointButton = new JButton("画点");
        lineButton = new JButton("画线");
        arcButton = new JButton("画弧");
        circleButton = new JButton("画圆");
        rectangleButton = new JButton("绘制矩形");

		saveButton = new JButton("保存文件");
		openButton = new JButton("打开文件");
		
        // 将保存和打开按钮添加到 filePanel
        filePanel.add(saveButton);
        filePanel.add(openButton);

        // 将绘制形状的按钮添加到 shapePanel
        shapePanel.add(pointButton);
        shapePanel.add(lineButton);
        shapePanel.add(arcButton);
        shapePanel.add(circleButton);
        shapePanel.add(rectangleButton);

        lineStyleComboBox = new JComboBox<>(new String[]{"实线", "虚线"});
        lineWidthSlider = new JSlider(1, 10, 1);
        colorButton = new JButton("选择颜色");

 		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 120, 5); // 设置组件间距
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lineWidthLabel = new JLabel("线型:");
        lineWidthLabel.setFont(largeFont); 
        optionsPanel.add(lineWidthLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        optionsPanel.add(lineStyleComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lineWidthLabel2 = new JLabel("线宽:");
        lineWidthLabel2.setFont(largeFont); // 为标签设置新字体
        optionsPanel.add(lineWidthLabel2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        optionsPanel.add(lineWidthSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        optionsPanel.add(colorButton, gbc);
        
        keyboardDrawingButton = new JButton("精确画点（键盘交互绘制）");
        keyboardDrawingButton.setFont(largeFont);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		optionsPanel.add(keyboardDrawingButton, gbc);
        
        // 创建一个总的工具栏面板并添加上面创建的三个面板
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(filePanel, BorderLayout.CENTER);
        toolbarPanel.add(shapePanel, BorderLayout.NORTH);
        toolbarPanel.add(optionsPanel, BorderLayout.SOUTH);
        // 将工具栏面板添加到主窗口
        add(toolbarPanel, BorderLayout.NORTH);

        
        // 为按钮、滑块和下拉框设置字体
        pointButton.setFont(largeFont);
        lineButton.setFont(largeFont);
        arcButton.setFont(largeFont);
        circleButton.setFont(largeFont);
        rectangleButton.setFont(largeFont);
        saveButton.setFont(largeFont);
        openButton.setFont(largeFont);
        colorButton.setFont(largeFont);

        lineWidthSlider.setFont(largeFont);
        lineStyleComboBox.setFont(largeFont);
        
        canvasImage = new BufferedImage(1800, 1400, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = canvasImage.createGraphics();
		g2d.setColor(Color.WHITE); // 设置背景颜色为白色
		g2d.fillRect(0, 0, getWidth(), getHeight()); // 填充整个图像
		g2d.dispose();

        // 绘图区域
        drawingPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
			    super.paintComponent(g);
			    g.drawImage(canvasImage, 0, 0, null);
			    if (showCoordinates) {
			        drawCoordinates(g);
			    }
			}
        };
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (pointButton.isSelected()) {
                    currentShape = new PointShape(e.getX(), e.getY(), selectedColor);
                } else if (lineButton.isSelected()) {
                    currentShape = new LineShape(e.getX(), e.getY(), selectedColor, getSelectedLineStyle(), lineWidthSlider.getValue());
                } else if (arcButton.isSelected()) {
                   currentShape = new ArcShape(e.getX(), e.getY(), selectedColor, getSelectedLineStyle(), lineWidthSlider.getValue());
                } else if (circleButton.isSelected()) {
                    currentShape = new CircleShape(e.getX(), e.getY(), selectedColor, getSelectedLineStyle(), lineWidthSlider.getValue());
                } else if (rectangleButton.isSelected()) {
                    currentShape = new RectangleShape(e.getX(), e.getY(), selectedColor, getSelectedLineStyle(), lineWidthSlider.getValue());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
				if (currentShape != null) {
				        Graphics2D g2d = canvasImage.createGraphics();
				        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				        currentShape.draw(g2d);
				        g2d.dispose();
				        shapes.add(currentShape);
				        currentShape = null;
				        drawingPanel.repaint();
				}
            }
        });
        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
			    if (currentShape != null) {
			        currentShape.updateEndPoint(e.getX(), e.getY());
			        drawingPanel.repaint(); // 只重绘面板，不修改 canvasImage
			    }
            }
        });

        // 添加组件到主界面
        add(toolbarPanel, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.WEST);
        add(drawingPanel, BorderLayout.CENTER);

        // 添加事件监听器
        pointButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtons();
                pointButton.setSelected(true);
            }
        });

        lineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtons();
                lineButton.setSelected(true);
            }
        });

		arcButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        resetButtons();
		        arcButton.setSelected(true);
		    }
		});
        
        circleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtons();
                circleButton.setSelected(true);
            }
        });

        rectangleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetButtons();
                rectangleButton.setSelected(true);
            }
        });

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(DrawingApp.this, "选择颜色", selectedColor);
                if (newColor != null) {
                    selectedColor = newColor;
                }
            }
        });
        
        keyboardDrawingButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	showCoordinates = true;
        		drawingPanel.repaint();
		        String inputX = JOptionPane.showInputDialog(DrawingApp.this, "请输入X坐标:");
		        String inputY = JOptionPane.showInputDialog(DrawingApp.this, "请输入Y坐标:");
		        try {
		            int x = Integer.parseInt(inputX);
		            int y = Integer.parseInt(inputY);
		            Graphics2D g2d = canvasImage.createGraphics();
		            g2d.setColor(selectedColor);
		            g2d.fillOval(x - 4, y - 4, 8, 8);
		            g2d.dispose();
		            drawingPanel.repaint();
		            showCoordinates = false;
		        } catch (NumberFormatException ex) {
		            JOptionPane.showMessageDialog(DrawingApp.this, "请输入有效的坐标！");
		        }
		    }
		});
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
		        if (canvasImage != null) {
		            JFileChooser fileChooser = new JFileChooser();
		            fileChooser.setDialogTitle("保存图形");
		            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG 图片", "png"));
		            if (fileChooser.showSaveDialog(DrawingApp.this) == JFileChooser.APPROVE_OPTION) {
		                File file = fileChooser.getSelectedFile();
		                String filePath = file.getAbsolutePath();
		                if (!filePath.toLowerCase().endsWith(".png")) {
		                    filePath += ".png";
		                }
		                try {
		                    ImageIO.write(canvasImage, "PNG", new File(filePath));
		                } catch (IOException ex) {
		                    ex.printStackTrace();
		                }
		            }
		        }
		    }
		});
			
		openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("打开图形");
                fileChooser.setFileFilter(new FileNameExtensionFilter("PNG 图片", "png"));
                if (fileChooser.showOpenDialog(DrawingApp.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        canvasImage = ImageIO.read(file); // 读取图像文件
                        drawingPanel.repaint(); // 重绘面板以显示图像
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        setVisible(true);

        shapes = new ArrayList<>();
        selectedColor = Color.BLACK;
    }
    
    public BufferedImage getCanvasImage() {
        return canvasImage;
    }

    private void resetButtons() {
        pointButton.setSelected(false);
        lineButton.setSelected(false);
        arcButton.setSelected(false);
        circleButton.setSelected(false);
        rectangleButton.setSelected(false);
    }

    private int getSelectedLineStyle() {
        return lineStyleComboBox.getSelectedIndex() == 0 ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DrawingApp();
            }
        });
    }

    private interface Shape extends Serializable{
        void draw(Graphics2D g2d);

        void updateEndPoint(int x, int y);
    }
    
    private void drawCoordinates(Graphics g) {
	    int width = drawingPanel.getWidth();
	    int height = drawingPanel.getHeight();
	
	    // 设置字体
	    Font font = new Font("Serif", Font.PLAIN, 12);
	    g.setFont(font);
	    
	    // 绘制坐标轴
	    g.setColor(Color.GRAY);
	    g.drawLine(0, 0, width, 0); // X轴
	    g.drawLine(0, 0, 0, height); // Y轴
	
	    // 绘制刻度（可根据需要调整刻度间距和大小）
	    for (int i = 0; i < width; i += 50) {
	        g.drawLine(i, 0, i, 10);
	        g.drawString(String.valueOf(i), i - 4, 20); // X轴
	    }
	    for (int i = 0; i < height; i += 50) {
	        g.drawLine(0, i, 10, i);
	        g.drawString(String.valueOf(i), 15, i + 4); // Y轴
	    }
	}

    private class PointShape implements Shape {
        private transient int x, y;
        private Color color;

        public PointShape(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public void draw(Graphics2D g2d) {
            int pointSize = 8; // 可以根据需要调整点的大小
        	g2d.setColor(color);
        	g2d.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
        }

        @Override
        public void updateEndPoint(int x, int y) {
            // 不需要更新
        }
    }

    private class LineShape implements Shape {
        private int startX, startY, endX, endY;
        private Color color;
        private int lineStyle;
        private int lineWidth;

        public LineShape(int startX, int startY, Color color, int lineStyle, int lineWidth) {
            this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
            this.color = color;
            this.lineStyle = lineStyle;
            this.lineWidth = lineWidth;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            // 设置虚线
            if (lineStyle == BasicStroke.CAP_BUTT) {
                float[] dashPattern = {10, 10}; // 可以根据需要调整虚线的模式
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
			g2d.drawLine(startX, startY, endX, endY);
        }

        @Override
        public void updateEndPoint(int x, int y) {
            endX = x;
            endY = y;
        }
    }

    private class ArcShape implements Shape {
        private int startX, startY, endX, endY;
        private Color color;
        private int lineStyle;
        private int lineWidth;
        //private int startAngle;
    	//private int arcAngle;

        public ArcShape(int startX, int startY, Color color, int lineStyle, int lineWidth) {
        	this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
            this.color = color;
            this.lineStyle = lineStyle;
            this.lineWidth = lineWidth;
            //this.startAngle = startAngle;
        	//this.arcAngle = arcAngle;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            // 设置虚线
            if (lineStyle == BasicStroke.CAP_BUTT) {
                float[] dashPattern = {10, 10}; // 可以根据需要调整虚线的模式
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            g2d.drawArc(x, y, width, height, 0, 180);
        }

        @Override
        public void updateEndPoint(int x, int y) {
            endX = x;
            endY = y;
        }
    }
    
    private class CircleShape implements Shape {
        private int startX, startY, endX, endY;
        private Color color;
        private int lineStyle;
        private int lineWidth;

        public CircleShape(int startX, int startY, Color color, int lineStyle, int lineWidth) {
            this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
            this.color = color;
            this.lineStyle = lineStyle;
            this.lineWidth = lineWidth;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            // 设置虚线
            if (lineStyle == BasicStroke.CAP_BUTT) {
                float[] dashPattern = {10, 10}; // 可以根据需要调整虚线的模式
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            g2d.drawArc(x, y, width, height, 0, 360);
        }

        @Override
        public void updateEndPoint(int x, int y) {
            endX = x;
            endY = y;
        }
    }

    private class RectangleShape implements Shape {
        private int startX, startY, endX, endY;
        private Color color;
        private int lineStyle;
        private int lineWidth;

        public RectangleShape(int startX, int startY, Color color, int lineStyle, int lineWidth) {
            this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
            this.color = color;
            this.lineStyle = lineStyle;
            this.lineWidth = lineWidth;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            // 设置虚线
            if (lineStyle == BasicStroke.CAP_BUTT) {
                float[] dashPattern = {10, 10}; // 可以根据需要调整虚线的模式
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(lineWidth, lineStyle, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            g2d.drawRect(x, y, width, height);
        }

        @Override
        public void updateEndPoint(int x, int y) {
            endX = x;
            endY = y;
        }
    }
}