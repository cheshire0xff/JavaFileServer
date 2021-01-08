package ConsoleClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import server.RemoteDirectory;

// TdbClient


/*
 *  takes console input and writes it back to the socket
 */

/*
 * communication protocol:
 * ? - request an answer
 * ! - no answer needed
 * $ - finish
 */


public class ConsoleClient {

    public static void listFiles(String tabs, RemoteDirectory dir)
    {
            for (var f : dir.files)
            {
                System.out.println(tabs + f.path);
            }
            for (var f : dir.dirs)
            {
                System.out.println(tabs + f.path);
                listFiles(tabs + "\t", f);
            }
    }
    public static void main(String[] args) {
        InetAddress serverAddress;
        try {
            if (args.length > 0)
            {
                serverAddress = InetAddress.getByName(args[0]);
            }
            else
            {
                serverAddress = InetAddress.getLocalHost();
            }
        } catch (UnknownHostException e) {
            
            System.out.println("Cannot obtain server ip!");
            return;
        }
        try (
                    Socket socket = new Socket(serverAddress, 5000);
                    Scanner scanner = new Scanner(System.in);       
                ){
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter socketWriter = new PrintWriter(socket.getOutputStream());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            socketWriter.print("getroot\n");
            socketWriter.flush();
            var objectInputStream = new ObjectInputStream(socket.getInputStream());
            RemoteDirectory rdir = (RemoteDirectory) objectInputStream.readObject();
            listFiles("", rdir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
