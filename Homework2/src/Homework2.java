/**
 * @(#)Homework2.java
 *
 * Homework2 application
 *
 * @author 
 * @version 1.00 2023/10/19
 */
import java.util.Scanner;

public class Homework2 {
    
    public static void main(String[] args) {
   		if (args.length == 0) {
   			Scanner scanner = new Scanner(System.in);
   			System.out.println("请正确输入学号和姓名"); 
            String number = scanner.next();
            String name = scanner.next();
            System.out.println("Hello, " + name);
        } else {
            System.out.println("Hello,"+args[1]);
        }
    }
}
