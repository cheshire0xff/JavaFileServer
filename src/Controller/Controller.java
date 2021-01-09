package Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;

import server.RemoteDirectory;
import server.RemoteFileInfo;
import server.TdpServer;

public class Controller 
{
    public RemoteDirectory rootDir;

    public Controller(InetAddress hostname) throws ClassNotFoundException, IOException
    {
        this.hostname = hostname;
        rootDir = getRoot();
    }

    public boolean uploadFile(String inputPath,  IDownloadProgressObserver observer) throws IOException
    {
        return false;
    }
    public boolean uploadDirectory(String directoryName ) throws IOException
    {
        return false;
    }
    
    public boolean delete(RemoteFileInfo file) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return delete("deletefile ", file.path);
    }
    public boolean delete(RemoteDirectory directory) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return delete("deletedir ", directory.path);
    }

    public boolean downloadFile(String outputPath, RemoteFileInfo file,  IDownloadProgressObserver observer) throws IOException
    {
        var currentSocket = getSocket();
        var writer = new PrintWriter(currentSocket.getOutputStream());
        writer.println("getfile " + file.path);
        writer.flush();
        var sizeLeft = file.sizeBytes;
        var fileOnDisk = new File(outputPath + Paths.get(file.path).getFileName());
        fileOnDisk.createNewFile();
        var fileInput = new FileOutputStream(fileOnDisk);
        while (sizeLeft > 0)
        {
            int chunkSize = TdpServer.downloadChunkSize;
            if (sizeLeft < chunkSize)
            {
                chunkSize = sizeLeft;
            }
            sizeLeft -= chunkSize;
            var buf = currentSocket.getInputStream().readNBytes((int) chunkSize);
            fileInput.write(buf);
            observer.updateProgress(file.sizeBytes - sizeLeft, file.sizeBytes);
        }
        fileInput.flush();
        fileInput.close();
        return Arrays.equals(file.calculateMD5(fileOnDisk.getPath()), file.md5digest);
    }

    private InetAddress hostname;
    private Socket socket = null;

    private RemoteDirectory getRoot() throws IOException, ClassNotFoundException
    {
            var currentSocket = getSocket();
            PrintWriter socketWriter = new PrintWriter(currentSocket.getOutputStream());
            socketWriter.print("getroot\n");
            socketWriter.flush();
            var objectInputStream = new ObjectInputStream(currentSocket.getInputStream());
            return (RemoteDirectory) objectInputStream.readObject();
    }
    
    private Socket getSocket() throws UnknownHostException, IOException
    {
        if (socket == null)
        {
            socket = new Socket(hostname, 5000);
        }
        return socket;
    }
    
    private boolean delete(String request, String path) throws UnknownHostException, IOException, ClassNotFoundException
    {
        var currentSocket = getSocket();
        var writer = new PrintWriter(currentSocket.getOutputStream());
        var reader = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
        writer.println(request  + path);
        writer.flush();
        var line = reader.readLine();
        rootDir = getRoot();
        if (line.equals("OK"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
