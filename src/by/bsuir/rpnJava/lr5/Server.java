package by.bsuir.rpnJava.lr5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());
    private ServerSocket serverSocket;

    public Server() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Constant.ENTER_PORT);
        int portNumber = scanner.nextInt();

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println(Constant.SERVER_STARTED + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();

                Connection con = new Connection(socket);
                connections.add(con);

                con.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.printf(Constant.SERVER_STOPPED);
            closeAll();
        }
    }


    private void closeAll() {
        try {
            serverSocket.close();

            synchronized(connections) {
                Iterator<Connection> iterator = connections.iterator();
                while(iterator.hasNext()) {
                    iterator.next().close();
                }
            }
        } catch (Exception e) {
            System.err.println(Constant.SOMETHING_WRONG);
        }
    }

    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = Constant.EMPTY_STRING;


        public Connection(Socket socket) {
            this.socket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }


        @Override
        public void run() {
            try {
                name = in.readLine();

                synchronized(connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + " cames now");
                    }
                }

                String str;
                while (true) {
                    str = in.readLine();
                    if(str.equals(Constant.EXIT)) break;


                    synchronized(connections) {
                        Iterator<Connection> iter = connections.iterator();
                        while(iter.hasNext()) {
                            ((Connection) iter.next()).out.println(name + ": " + str);
                        }
                    }
                }

                synchronized(connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + Constant.LEFT_THE_CHAT);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        public void close() {
            try {
                in.close();
                out.close();
                socket.close();


                connections.remove(this);
                if (connections.size() == 0) {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println(Constant.SOMETHING_WRONG);
            }
        }
    }
}
