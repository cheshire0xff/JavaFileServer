package ConsoleClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Scanner;

import Controller.Controller;
import server.IObserver;
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

class Observer implements IObserver
{
    Observer(String text)
    {
        this.text = text;
    }
    String text;
    @Override
    public void updateProgress(int downloaded, int total) {
        System.out.println(text + downloaded + "/" + total + " bytes");
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
            var fileToDownload = controller.rootDir.files.get(3);
            var filenameToDownload = Paths.get(fileToDownload.path).getFileName();
            var ok = controller.downloadFile("/home/cheshire/JavaFileServerLocal/" + filenameToDownload, fileToDownload,  new Observer("Downloading "));
            System.out.println("MD5 is " + (ok ? "ok" : "incorrect"));
            ok = controller.delete(controller.rootDir.dirs.get(0));
            System.out.println("file delete: " + (ok ? "ok" : "incorrect"));
            ok = controller.uploadFile("/home/cheshire/JavaFileServerLocal/" + filenameToDownload, "/home/cheshire/JavaFileServer/testDir1/aa_copy.jpeg", new Observer("Uploading "));
            listFiles("", controller.rootDir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
