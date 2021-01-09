package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static RemoteDirectory rootDir = null;

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
    
    public static void sendFile(Socket s, String path, IObserver observer) throws IOException
    {
        File f = new File(path);
        var fileInput = new FileInputStream(f);
        int size = (int) Files.size(Paths.get(path));
        int totalSize = size;
        var output = s.getOutputStream();
        while (size > 0)
        {
            int chunk = TdpServer.downloadChunkSize; // max size
            if (size < chunk)
            {
                chunk = size;
            }
            size -= chunk;
            var buf = fileInput.readNBytes((int) chunk);
            output.write(buf);
            output.flush();
            if (observer != null)
            {
                observer.updateProgress(totalSize  - size, totalSize);
            }
        }
        fileInput.close();
    }
    public static void receiveFile(Socket s, String path, int totalSize, IObserver observer) throws IOException
    {        
         var fileOnDisk = new File(path);
        fileOnDisk.createNewFile();
        var fileOutput = new FileOutputStream(fileOnDisk);
        int sizeLeft = totalSize;
        var socketInput = s.getInputStream(); 
        while (sizeLeft > 0)
        {
            int chunkSize = TdpServer.downloadChunkSize;
            if (sizeLeft < chunkSize)
            {
                chunkSize = sizeLeft;
            }
            sizeLeft -= chunkSize;
            var buf = socketInput.readNBytes((int) chunkSize);
            fileOutput.write(buf);
            if (observer != null)
            {
                observer.updateProgress(totalSize  - sizeLeft, totalSize);
            }
        }
        fileOutput.flush();
        fileOutput.close();
    }
    
    public static synchronized void syncFileList()
    {
        fillDir(Paths.get(TdpServer.rootDir.path));
    }
    
    public static synchronized RemoteDirectory getRoot()
    {
        return TdpServer.rootDir;
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
                rootDir.addFile(new RemoteFileInfo(f.toString()));
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
