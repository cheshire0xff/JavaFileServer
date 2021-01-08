package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


/*
 * handles tdp request dispatch and
 * creating file informations
 */


public class TdpServer {
    
    private final static int maxConnections = 10;
    public final static int downloadChunkSize = 100000;
    public final static int port = 5000;
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
        try(ServerSocket serverSocket = new ServerSocket(port))
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
            try
            {
                
            if (Files.isRegularFile(f))
            {
                rootDir.addFile(new RemoteFileInfo(f.toString(), (int) Files.size(f)));
            }
            else if (Files.isDirectory(f))
            {
                var dir = new RemoteDirectory(f.toString());
                rootDir.addDir(dir);
                var stream = Files.list(f);
                stream.forEach(new ConsumerImpl(dir));
                stream.close();
            }
            }
            catch(IOException e)
            {
                    e.printStackTrace();
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
