/**
 * @(#)Homework2_2.java
 *
 * Homework2_2 application
 *
 * @author 
 * @version 1.00 2023/11/2
 */
 import java.util.Scanner;
 
public class Homework2_2 {
 
    public static int partition(int []nums, int i, int j) {
        int t = nums[i];
        while(i<j) {
            while(i<j && nums[j]>=t) {
                j--;
            }
            nums[i] = nums[j];
            while(i<j && nums[i]<=t) {
                i++;
            }
            nums[j] = nums[i];
        }
        nums[i] = t;
        return i;
    }
 
    public static int quickSelect(int []nums, int i, int j, int index) {
        if(i>=j) {
            return nums[i];
        }
        int p = partition(nums, i, j);
        if(p == index) {
            return nums[p];
        } else if(p < index) {
            return quickSelect(nums, p+1, j, index);
        } else {
            return quickSelect(nums, i, p-1, index);
        }
    }
    
    public static int findKthLargest(int[] nums, int k) {
        return quickSelect(nums, 0, nums.length-1, nums.length-k);
    }

    public static void main(String[] args) {
    
	    Scanner sc = new Scanner(System.in);
	    
	 	System.out.print("请输入数组的长度：");
		int a = sc.nextInt();
		int [] nums = new int[a];
	
	    System.out.println("请输入数组：");
	
		for(int i = 0; i < a; i++) {
			nums[i] = sc.nextInt();
		}
		
	    System.out.print("请输入K的值为：");
		int k = sc.nextInt();
		sc.close();
		
		int ans = findKthLargest(nums, k);	
		System.out.println("第K大的数字是：" + ans);
		}

}
