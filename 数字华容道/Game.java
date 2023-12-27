import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

public class Game {
    	
	private JFrame frame;
    private JPanel panel;
    private JButton[] buttons;
    private JButton startButton, resetButton;
    private int moves;//当前步数
    private int emptyIndex; // 空白格的索引
    private ArrayList<Integer> numbers; // 存储数字
    private Map<String, Integer> scoreBoard; // 记录玩家得分
    private String playerName; //玩家昵称
    private JLabel statusLabel; // 用于显示状态信息的标签
	public static int[][] a;//用于存放4*4二维数组，将其放入puzzlesolver
	public static int minSteps; //用于存放PuzzleSolver中计算出的minSteps
	
    private void initialize() {
    	a = new int[4][4];
        // 窗口设置
        frame = new JFrame("Puzzle Game");
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	// 创建用于数字按钮的面板
    	JPanel gridPanel = new JPanel(new GridLayout(4, 4));
    	// 创建用于控制按钮的面板
    	JPanel controlPanel = new JPanel(new FlowLayout());

        // 初始化按钮
        buttons = new JButton[16];
        for (int i = 0; i < 16; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(new Font("Arial", Font.BOLD, 40));
            gridPanel.add(buttons[i]);
        }

        startButton = new JButton("开始");
        resetButton = new JButton("重置");
        
        startButton.setFont(new Font("宋体", Font.BOLD, 30));
		resetButton.setFont(new Font("宋体", Font.BOLD, 30));
		startButton.setPreferredSize(new Dimension(120, 50));
		resetButton.setPreferredSize(new Dimension(120, 50));
        
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        
    	controlPanel.add(startButton);
    	controlPanel.add(resetButton);
    	
        frame.add(gridPanel, BorderLayout.CENTER);
    	frame.add(controlPanel, BorderLayout.SOUTH);
    	
    	// 创建状态标签
    	statusLabel = new JLabel("Moves");
    	statusLabel.setFont(new Font("宋体", Font.BOLD, 25));
    	controlPanel.add(statusLabel);
        
        // 初始化数字列表
        numbers = new ArrayList<Integer>();
		PuzzleGenerator.generate();
		numbers.clear();
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 4; j++) {
		        numbers.add(a[i][j]);
		    }
		}
	    PuzzleSolver.solve(); //算出最小移动次数minSteps
	    emptyIndex = numbers.indexOf(0);
		moves = 0;
        scoreBoard = new LinkedHashMap<String, Integer>();
        startButton.setVisible(true);
        frame.setVisible(true);
	    updateButtons();
		updateStatus();
	    updateStartButtonState();
	    
	    for (int i = 0; i < 16; i++) {
            buttons[i].setEnabled(false);
		}
	
	    
	    // 添加事件监听器
        for (int i = 0; i < 16; i++) {
            final int index = i;
            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canMove(index)) {
                        move(index);
                        if (isSolved()) {
                            JOptionPane.showMessageDialog(frame, "恭喜您，游戏完成！");
                            //询问昵称
                            askForPlayerName();
					        // 更新得分
					        updateScoreBoard();
					        // 加载和显示排行榜
					        loadScoreBoard();
					        showScoreBoard();
					        // 保存排行榜
					        saveScoreBoard();
                        }
                    }
                }
            });
        }
    	


    }

    private void askForPlayerName() {
        playerName = JOptionPane.showInputDialog(frame, "输入你的游戏昵称，加入排行榜:");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Unknown Player";
        }
    }
    
	private boolean isSolvable() {
	    int inversions = 0;
	    int tot = 0;
	    int[] puz = new int[16];
    	for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                puz[tot] = a[i][j];
                tot++;
            }
        }
        for (int i = 0; i < 16; ++i) {
            if (puz[i] == 0)
                inversions += 3 - i / 4;
            else {
		        for (int j = 0; j < i; j++) {
	                if (puz[j] > puz[i] && puz[j] != 0) {
	                    inversions++;
		            }
		        }
    		}
        }
    	return (inversions % 2 == 0);
    	// 如果逆序数是偶数，则该排列是可解的
	}

    private void startGame() {
		startButton.setVisible(false);
		for (int i = 0; i < 16; i++) {
            buttons[i].setEnabled(true);
		}
    }
    
	private void updateStartButtonState() {
    if (isSolvable()&&(minSteps < 45)) {
        startButton.setEnabled(true);
        startButton.setBackground(Color.GREEN);
    } else {
        startButton.setEnabled(false);
        startButton.setBackground(Color.GRAY);
    	}
	}
	
	private void resetGame() {
	    PuzzleGenerator.generate();
	    numbers.clear();
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 4; j++) {
		        numbers.add(a[i][j]);
		    }
		}
	    PuzzleSolver.solve(); //算出最小移动次数minSteps
	    
	    for (int i = 0; i < 16; i++) {
            buttons[i].setEnabled(false);
		}
        emptyIndex = numbers.indexOf(0);
	    moves = 0;
	    updateButtons();
	    startButton.setVisible(true);
	    updateStartButtonState();
	    updateStatus();
	}

	private void enableButtons(boolean enable) {
	    for (JButton button : buttons) {
	        button.setEnabled(enable);
	    }
	}

    private boolean canMove(int index) {
        // 检查是否可以移动
        return Math.abs(index - emptyIndex) == 1 || Math.abs(index - emptyIndex) == 4;
    }

    private void move(int index) {

        // 交换位置
        Collections.swap(numbers, index, emptyIndex);
        emptyIndex = index;
        updateButtons();
        moves++;
        updateStatus();
    }
    
    private void updateScoreBoard() {
	    int score = (100 * minSteps) / Math.max(moves, 1); // 确保不除以零
	    scoreBoard.put(playerName, score);
	}
	
    private void loadScoreBoard() {
	    try {
	        File file = new File("scoreboard.txt");
	        if (file.exists()) {
	            BufferedReader reader = new BufferedReader(new FileReader(file));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                String[] parts = line.split(",");
	                if (parts.length == 2) {
	                    scoreBoard.put(parts[0], Integer.parseInt(parts[1]));
	                }
	            }
	            reader.close();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
    private void showScoreBoard() {

		java.util.List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>();
		entries.addAll(scoreBoard.entrySet());
	    // 按照分数降序排序
	    Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
		    @Override
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
		        return o2.getValue().compareTo(o1.getValue());
		    }
		});
	
	    StringBuilder sb = new StringBuilder("<html><h1 style='text-align: center;font-size: 30px;'>排行榜</h1><table>");
	    for (Map.Entry<String, Integer> entry : entries) {
	        sb.append(entry.getKey()).append("</td><td style='text-align: right;'>").append(entry.getValue()).append("</td></tr>");
	    }
		sb.append("</table></html>");
		
	    // 显示排行榜
	    JOptionPane.showMessageDialog(frame, sb.toString(), "Score Board", JOptionPane.INFORMATION_MESSAGE);
        
    }
    
    private void saveScoreBoard() {
	    try {
	        File file = new File("scoreboard.txt");
	        FileWriter writer = new FileWriter(file, false); // false 表示不追加，每次都重写文件
	
	        for (Map.Entry<String, Integer> entry : scoreBoard.entrySet()) {
	            writer.write(entry.getKey() + "," + entry.getValue() + "\n");
	        }
	
	        writer.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	
	private void updateButtons() {
        // 更新按钮显示
        for (int i = 0; i < 16; i++) {
            if (numbers.get(i) == 0) {
                buttons[i].setText("");
            } else {
                buttons[i].setText(String.valueOf(numbers.get(i)));
            }
        }
    }
    
    private void updateStatus() {
	    // 更新状态标签的文本
	    if (isSolvable()&&(minSteps < 45)){
	    	statusLabel.setText("    当前步数: " + moves+"    最小步数: " + minSteps);
	    }else{
	    	statusLabel.setText("  当前状态不可解或最小步数超出范围  " );
	    }
	}


    private boolean isSolved() {
        // 检查是否解决
        for (int i = 0; i < 15; i++) {
            if (numbers.get(i) != i + 1) {
                return false;
            }
        }
        return true;
    }

    public void show() {
        frame.setVisible(true);
    }
    
    public Game() {
        initialize();
    }
    
    public static void main(String[] args) {
    	// 设置字体大小
    	UIManager.put("OptionPane.messageFont", new Font("宋体", Font.BOLD, 30));
        UIManager.put("OptionPane.buttonFont", new Font("宋体", Font.PLAIN, 25));
        UIManager.put("OptionPane.textFieldFont", new Font("Arial", Font.BOLD, 25));
        // 在事件调度线程中运行应用程序
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Game().show();
            }
        });
    }
}

