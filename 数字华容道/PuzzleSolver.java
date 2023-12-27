import java.util.Scanner;

public class PuzzleSolver {
    static int x, y, cnt;
    static int[][] tmp = new int[5][5];
    static int[] puz = new int[16];

    static final int[][] goal = {
        {0, 0, 0, 0, 0},
        {0, 1, 2, 3, 4},
        {0, 5, 6, 7, 8},
        {0, 9, 10, 11, 12},
        {0, 13, 14, 15, 0}
    };

    static final int[] dx = {0, 1, -1, 0};
    static final int[] dy = {1, 0, 0, -1};

    static final int[] px = {4, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4};
    static final int[] py = {4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3};

    private static int mabs(int a) {
        return Math.abs(a);
    }

    private static int evaluate() {
        int res = 0;
        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 4; j++) {
                if (tmp[i][j] == 0) continue;
                res += mabs(i - px[tmp[i][j]]) + mabs(j - py[tmp[i][j]]);
            }
        }
        return res;
    }

    private static int getway(int i) {
        if (dx[i] == -1) return 2;
        if (dx[i] == 1) return 1;
        if (dy[i] == -1) return 4;
        if (dy[i] == 1) return 3;
        return 0;
    }

    private static boolean test(int i, int pre) {
        return (dx[i] == -1 && pre == 1) || (dx[i] == 1 && pre == 2) || (dy[i] == -1 && pre == 3) || (dy[i] == 1 && pre == 4);
    }

    private static boolean IDA_star(int step, int x, int y, int pre) {
        int eva = evaluate();
        if (eva == 0) return true;

        if (step + eva > cnt) return false;

        for (int i = 0; i < 4; i++) {
            int xx = x + dx[i];
            int yy = y + dy[i];
            if (xx > 4 || xx < 1 || yy > 4 || yy < 1 || test(i, pre)) continue;
            int temp = tmp[xx][yy];
            tmp[xx][yy] = tmp[x][y];
            tmp[x][y] = temp;
            if (IDA_star(step + 1, xx, yy, getway(i))) return true;
            tmp[x][y] = tmp[xx][yy];
            tmp[xx][yy] = temp;
        }
        return false;
    }

    public static void solve() {
    	
    	cnt=0;
    	
		for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tmp[i + 1][j + 1] = Game.a[i][j];
                if (tmp[i + 1][j + 1] == 0) {
                    x = i + 1;
                    y = j + 1;
                }
            }
        }

        if (evaluate() == 0) {
            return;
        }


        while (++cnt <= 45) { //搜索深度限制小于等于45
            if (IDA_star(0, x, y, 0)) {
                Game.minSteps=cnt;
                return;
            }
        }
    }
}
