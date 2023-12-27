/**
 * @(#)Homework3.java
 *
 * Homework3 application
 *
 * @author 
 * @version 1.00 2023/10/20
 */
 
import java.util.Scanner;
public class Homework3 {
    
    public static long CountWays(long n1) {
    	long n = n1+1;
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        long temp1 = 0;
       	long temp2 = 1;
        long res = 1;
        for (long j=2;j<=n;j++){
        	res = temp1 + temp2;
        	temp1 = temp2;
        	temp2 = res;
        }
        return res;
    }
    
    public static void main(String[] args) {
        
		System.out.print("请输入n：");
		Scanner scanner = new Scanner(System.in);
		long n = scanner.nextLong();

		long count = CountWays(n);
		System.out.println("走法数量: " + count);
		
		long maxN_Byte = 0;
        long maxN_Short = 0;
        long maxN_Int = 0;
        long maxN_Long = 0;
        long i = 0;
        long a,b,c,d = 0;
        
        do{
        	i++;
        	a = CountWays(i)-Byte.MAX_VALUE;
	
        }while (a<0);
        maxN_Byte=i-1;
        System.out.println("byte " + maxN_Byte);
        
        i=0;
        do{
        	i++;
        	b = CountWays(i)-Short.MAX_VALUE;
        	
        }while (b<0);
        maxN_Short=i-1;
        System.out.println("short " + maxN_Short);
        
        i=0;
        do{
        	i++;
        	c = CountWays(i)-Integer.MAX_VALUE;
        	
        }while (c<0);
        maxN_Int=i-1;
        System.out.println("int " + maxN_Int);
        
        i=0;
        do{
        	i++;
        	d = CountWays(i)-Long.MAX_VALUE;
        	
        }while (d<0);
        maxN_Long=i-1;
        System.out.println("long " + maxN_Long);
    }

}
