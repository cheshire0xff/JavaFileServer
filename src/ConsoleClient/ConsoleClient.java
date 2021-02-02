package ConsoleClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import ClientApi.ClientApi;
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
    public void updateProgress(long downloaded, long total) {
        System.out.print(text + downloaded + "/" + total + " bytes\r");
        System.out.flush();
    }
    
}

public class ConsoleClient {
    
    static final String helpMessage = 
            "! - any commands staring with ! are redirected to system console\n" + 
            "h - help\n" + 
            "q - exit\n" + 
            "ls - list files\n" + 
            "download REMOTE_PATH LOCAL_PATH\n" + 
                "\tdownload file from server\n" + 
                "\tREMOTE_PATH - relative path to a file on remote server\n" + 
                "\tLOCAL_PATH - path on local machine where file will be downloaded\n" + 
            "upload LOCAL_PATH REMOTE_PATH\n" + 
                "\tupload file to server\n" + 
                "\tLOCAL_PATH - path on local machine with file to be uploaded\n" + 
                "\tREMOTE_PATH - relative path on remote server, where file will be saved\n" + 
            "rm REMOTE_PATH\n" + 
                "\tREMOTE_PATH - relative path to remote file you want to remove\n" + 
            "mkdir REMOTE_PATH\n" + 
                "\tREMOTE_PATH - relative path on remote server, where dir will be created\n" + 
            "rmdir REMOTE_PATH\n" + 
                "\tREMOTE_PATH - relative path to remote dir you want to remove\n";

    static void help()
    {
        System.out.print(helpMessage);
    }
    static void ls(RemoteDirectory pwd, String tabs)
    {
            for (var f : pwd.files)
            {
                System.out.println(tabs + f.filename);
            }
            for (var f : pwd.dirs)
            {
                System.out.println(tabs + f.directoryName);
                ls(f, tabs + "\t");
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
        help();
        try (
                    var clientApi = new ClientApi(serverAddress);
                    Scanner scanner = new Scanner(System.in);       
                ){
            while (true)
            {
                var line = scanner.nextLine();
                if (line.startsWith("!"))
                {
                    var process = Runtime.getRuntime().exec(line.substring(1));
                    var scn = new Scanner(process.getInputStream());
                    while(scn.hasNextLine())
                    {
                        System.out.println(scn.nextLine());
                    }
                    scn.close();
                }
                else if (line.equals("h"))
                {
                    help();
                }
                else if (line.equals("q"))
                {
                    System.out.println("quitting");
                    return;
                }
                else if (line.equals("ls"))
                {
                    ls(clientApi.rootDir, "");
                }
                else if (line.startsWith("download "))
                {
                    args = line.split(" ", 3);
                   if ( clientApi.downloadFile(args[2],args[1], new Observer("Downloading")))
                   {
                       System.out.println("Download ok.");
                   }
                   else
                   {
                       System.out.println("Download failed.");
                   }
                }
                else if (line.startsWith("upload "))
                {
                    args = line.split(" ", 3);
                    if (clientApi.uploadFile(args[1],args[2], new Observer("Uploading")))
                    {
                       System.out.println("Upload ok.");
                    }
                    else
                    {
                       System.out.println("Upload failed.");
                    }
                }
                else if (line.startsWith("mkdir "))
                {
                    args = line.split(" ", 2);
                    if (clientApi.uploadDirectory(args[1]))
                    {
                       System.out.println("mkdir ok.");
                    }
                    else
                    {
                       System.out.println("mkdir failed.");
                    }
                }
                else if (line.startsWith("rmdir "))
                {
                    args = line.split(" ", 2);
                    if (clientApi.deleteDir(args[1]))
                    {
                       System.out.println("rmdir ok.");
                    }
                    else
                    {
                       System.out.println("rmdir failed.");
                    }
                }
                else if (line.startsWith("rm "))
                {
                    args = line.split(" ", 2);
                    if (clientApi.deleteFile(args[1]))
                    {
                       System.out.println("rm ok.");
                    }
                    else
                    {
                       System.out.println("rm failed.");
                    }
                }
            }
        } catch (Exception  e) {
            e.printStackTrace();
        }
    }

}
