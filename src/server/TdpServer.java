package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


class RequestHandler implements Runnable
{
    public RequestHandler(Socket socket) {
        this.socket = socket;
    }
    private Socket socket;
    @Override
    /*
     * request:
     * getroot
     *      - returns root directory with it's content (RemoteDirectory)
     *  getfile PATH
     *      - return raw bytes from a file
     */
    
    public void run() {
        while(true)
        {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("start reading");
                String line = reader.readLine();
                if (line == null)
                {
                    return;
                }
                System.out.println("read: " + line);
                if (line.equals("getroot"))
                {
                    var objectStream = new ObjectOutputStream(socket.getOutputStream());
                    objectStream.writeObject(TdpServer.rootDir);
                }
                else if (line.startsWith("getfile "))
                {
                    var path = line.split(" ", 2)[1];
                    if (!path.startsWith(TdpServer.rootDir.path))
                    {
                        System.out.println("Invalid path, not in root");
                        return;
                    }
                    var p = Paths.get(path);
                    if (!Files.isRegularFile(p))
                    {
                        System.out.println("File not found");
                        return;
                    }
                    File f = new File(p.toString());
                    var fileInput = new FileInputStream(f);
                    int size = (int) Files.size(p);
                    while (size > 0)
                    {
                        int chunk = 10000; // max size
                        if (size < chunk)
                        {
                            chunk = size;
                        }
                        size -= chunk;
                        var buf = fileInput.readNBytes((int) chunk);
                        socket.getOutputStream().write(buf);
                        socket.getOutputStream().flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class TdpServer {
    
    private final static int maxConnections = 10;
    public static RemoteDirectory rootDir = null;

    public static void main(String[] args) {
        if (args.length != 1)
        {
            System.err.println("Provide root dir path");
            System.exit(-1);
        }
        var path = Paths.get(args[0]);
        if (!Files.isDirectory(path))
        {
            System.err.println("File is not a directory!");
            System.exit(-3);
        }
        fillDir(path);
        try(ServerSocket serverSocket = new ServerSocket(5000))
        {
            ExecutorService exec = Executors.newFixedThreadPool(maxConnections);
            while(true)
            {
                Socket sock = serverSocket.accept();
                System.out.println("New accepted socket: " + sock.getPort());
                exec.submit(new RequestHandler(sock));
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    
    private static class ConsumerImpl implements Consumer<Path>
    {
        ConsumerImpl(RemoteDirectory dir)
        {
            this.rootDir = dir;
        }
        RemoteDirectory rootDir;
        @Override
        public void accept(Path f) {
            if (Files.isRegularFile(f))
            {
                try {
                    rootDir.addFile(new RemoteFileInfo(f.toString(), (int) Files.size(f)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (Files.isDirectory(f))
            {
                var dir = new RemoteDirectory(f.toString());
                rootDir.addDir(dir);
                try {
                    var stream = Files.list(f);
                    stream.forEach(new ConsumerImpl(dir));
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    public static void fillDir(Path path)
    {
        rootDir = new RemoteDirectory(path.toString());
        try {
            Files.list(path).forEach(new ConsumerImpl(rootDir));;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
