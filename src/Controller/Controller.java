package Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;

import server.IObserver;
import server.RemoteDirectory;
import server.RemoteFileInfo;
import server.TdpServer;

public class Controller implements AutoCloseable
{
    public RemoteDirectory rootDir;

    public Controller(InetAddress hostname) throws ClassNotFoundException, IOException
    {
        this.hostname = hostname;
        refresh();
        socket.setSoTimeout(10000);
    }
    
    public void refresh() throws ClassNotFoundException, IOException
    {
        rootDir = getRoot();
    }

    public boolean uploadFile(String inputPath, String serverpath,  IObserver observer) throws IOException, ClassNotFoundException
    {
        checkSocket();
        output.writeObject("upfile " + serverpath);
        output.flush();

        var upFile = new RemoteFileInfo(Paths.get(inputPath));
        output.writeObject(upFile);
        if (!checkOk())
        {
            return false;
        }
        TdpServer.sendFile(socket, inputPath, observer);
        var ok = checkOk();
        refresh();
        return ok;
    }
    public boolean uploadDirectory(String directoryName ) throws IOException, ClassNotFoundException
    {
        return singleRequest("updir, ", directoryName);
    }
    
    public boolean deleteFile(String serverPath) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return singleRequest("deletefile " , serverPath);
    }
    public boolean deleteDir(String serverPath) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return singleRequest("deletedir ", serverPath);
    }

    public boolean downloadFile(String outputPath, String serverPath,  IObserver observer) throws IOException, ClassNotFoundException
    {
        var remoteFile = rootDir.tryFindFile(serverPath);
        if (remoteFile == null)
        {
            return false;
        }
        checkSocket();
        output.writeObject("getfile " + serverPath);
        output.flush();
        if (!checkOk())
        {
            return false;
        }
        TdpServer.receiveFile(socket, outputPath, remoteFile.sizeBytes, observer);
        return Arrays.equals(remoteFile.calculateMD5(Paths.get(outputPath)), remoteFile.md5digest);
    }

    private InetAddress hostname;
    private Socket socket = null;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;

    private RemoteDirectory getRoot() throws IOException, ClassNotFoundException
    {
            checkSocket();
            output.writeObject("getroot");
            output.flush();
            return (RemoteDirectory) input.readObject();
    }
    
    private void checkSocket() throws UnknownHostException, IOException
    {
        if (socket == null)
        {
            socket = new Socket(hostname, 5000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        }
    }
    
    private boolean checkOk() throws ClassNotFoundException, IOException
    {
        var line = (String)input.readObject();
        if (line.equals("OK"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private boolean singleRequest(String request, String path) throws UnknownHostException, IOException, ClassNotFoundException
    {
        checkSocket();
        output.writeObject(request  + path);
        output.flush();
        var ok = checkOk();
        refresh();
        return ok;
    }

    @Override
    public void close() throws Exception {
        if (socket != null)
        {
            socket.close();
        }
    }

}
