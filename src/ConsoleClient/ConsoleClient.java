package ConsoleClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import Controller.Controller;
import Controller.IDownloadProgressObserver;
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

class Observer implements IDownloadProgressObserver 
{

    @Override
    public void updateProgress(int downloaded, int total) {
        System.out.println(downloaded + "/" + total + " bytes");
    }
    
}


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
                    Scanner scanner = new Scanner(System.in);       
                ){
            var controller = new Controller(serverAddress);
            listFiles("", controller.rootDir);
            var ok = controller.downloadFile("/home/cheshire/JavaFileServerLocal/", controller.rootDir.files.get(3),  new Observer());
            System.out.println("MD5 is " + (ok ? "ok" : "incorrect"));
            ok = controller.delete(controller.rootDir.dirs.get(0));
            System.out.println("file delete: " + (ok ? "ok" : "incorrect"));
            listFiles("", controller.rootDir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
