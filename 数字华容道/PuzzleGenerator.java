import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class PuzzleGenerator {
    private static final int SIZE = 4;
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int MAX_STEPS = 200; //限制随机步数最大为200
    private static ArrayList<Integer> number; // 存储数字
	public static int[][] puzzle = {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {9, 10, 11, 12},
            {13, 14, 15, 0}
        };
        
    private static boolean isValidMove(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public static void generate() {
    	
    	Random random = new Random();
    	boolean randomBoolean = random.nextBoolean();//创建布尔值随机数，用于倒序生成华容道或随机生成华容道
    	
    	if (randomBoolean){
	    	puzzle = new int[][] {
	            {1, 2, 3, 4},
	            {5, 6, 7, 8},
	            {9, 10, 11, 12},
	            {13, 14, 15, 0}
	        };
	        int blankX = SIZE - 1, blankY = SIZE - 1;
	        Random rand = new Random();
	
	        for (int step = 0; step < MAX_STEPS; step++) {
	            int move = rand.nextInt(DIRECTIONS.length);
	            int newX = blankX + DIRECTIONS[move][0];
	            int newY = blankY + DIRECTIONS[move][1];
	
	            if (isValidMove(newX, newY)) {
	                puzzle[blankX][blankY] = puzzle[newX][newY];
	                puzzle[newX][newY] = 0;
	                blankX = newX;
	                blankY = newY;
	            }
	        }
	        for (int i = 0; i < 4; i++) {
			    for (int j = 0; j < 4; j++) {
			        Game.a[i][j] = puzzle[i][j];
			    }
			}
	    }else{
	    	number = new ArrayList<Integer>();
	        for (int i = 0; i < 16; i++) {
	            number.add(i);
	        }
	
	        // 随机打乱这个列表
	        Collections.shuffle(number);
	
	        // 将随机数字填充到二维数组中
	        int index = 0;
	        for (int i = 0; i < 4; i++) {
	            for (int j = 0; j < 4; j++) {
	                Game.a[i][j] = number.get(index);
	                index++;
	            }
	        }
	    }
    }
}