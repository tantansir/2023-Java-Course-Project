/**
 * @(#)Homework2_1.java
 *
 * Homework2_1 application
 *
 * @author 
 * @version 1.00 2023/11/2
 */
 import java.util.Scanner;
 
public class Homework2_1 {
	public static int getMaxarea(int height[]){
        int max_area = 0;
        int area = 0;
        int left;
        int right;
        for(int i=0; i<height.length; i++){
            left = i;
            right = i;
            while(left>0 && height[left-1] >= height[i]){
                left--;
            }
            while(right<height.length-1 && height[right+1] >= height[i]){
                right++;
            }
            area = height[i] * (right-left+1);
            if(area > max_area){
                max_area = area;
            }
        }
        return max_area;
    }

    public static void main(String[] args) {
    	
		Scanner sc = new Scanner(System.in);
		System.out.print("请输入整型序列中数字的个数：");
        int a = sc.nextInt();
        int height[] = new int[a];
        System.out.println("请输入整型序列：");
        for(int i=0; i<a; i++){
            height[i] = sc.nextInt();
        }
        int max_area = getMaxarea(height);
        System.out.print("能够构成最大矩形框的面积为："+max_area);
        sc.close();
    }
}
