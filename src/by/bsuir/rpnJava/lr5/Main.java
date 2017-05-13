package by.bsuir.rpnJava.lr5;

import java.util.Scanner;

public class Main {
        public static void main(String[] args) {
            Scanner in = new Scanner(System.in);

            System.out.println(Constant.CHOOSE_MODE);
            while (true) {
                char answer = Character.toLowerCase(in.nextLine().charAt(0));
                if (answer == Constant.CHAR_SERVER_S) {
                    new Server();
                    break;
                } else if (answer == Constant.CHAR_CLIENT_C) {
                    new Client();
                    break;
                } else {
                    System.out.println(Constant.TRY_AGAIN_MESSAGE);
                }
            }
        }
}
