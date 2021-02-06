package ClientApi;

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

public class ClientApi implements AutoCloseable
{
    public RemoteDirectory rootDir;

    /**
     * @param hostname
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public ClientApi(InetAddress hostname) throws ClassNotFoundException, IOException
    {
        this.hostname = hostname;
        refresh();
        socket.setSoTimeout(10000);
    }

    /**
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void refresh() throws ClassNotFoundException, IOException
    {
        rootDir = getRoot();
    }

    /**
     * @param inputPath
     * @param serverpath
     * @param observer
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
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

    /**
     * @param directoryName \
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean uploadDirectory(String directoryName ) throws IOException, ClassNotFoundException
    {
        return singleRequest("updir, ", directoryName);
    }

    /**
     * @param serverPath
     * @return
     * @throws UnknownHostException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean deleteFile(String serverPath) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return singleRequest("deletefile " , serverPath);
    }

    /**
     * @param serverPath
     * @return
     * @throws UnknownHostException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean deleteDir(String serverPath) throws UnknownHostException, IOException, ClassNotFoundException
    {
        return singleRequest("deletedir ", serverPath);
    }

    /**
     * @param outputPath
     * @param serverPath
     * @param observer
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
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

    /**
     * @throws Exception
     */
    @Override
    public void close() throws Exception 
    {
        if (socket != null)
        {
            socket.close();
        }
    }


    private InetAddress hostname;
    private Socket socket = null;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;

    /**
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private RemoteDirectory getRoot() throws IOException, ClassNotFoundException
    {
            checkSocket();
            output.writeObject("getroot");
            output.flush();
            return (RemoteDirectory) input.readObject();
    }

    /**
     * @throws UnknownHostException
     * @throws IOException
     */
    private void checkSocket() throws UnknownHostException, IOException
    {
        if (socket == null)
        {
            socket = new Socket(hostname, 5000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        }
    }

    /**
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
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

    /**
     * @param request
     * @param path
     * @return
     * @throws UnknownHostException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private boolean singleRequest(String request, String path) throws UnknownHostException, IOException, ClassNotFoundException
    {
        checkSocket();
        output.writeObject(request  + path);
        output.flush();
        var ok = checkOk();
        refresh();
        return ok;
    }


}
