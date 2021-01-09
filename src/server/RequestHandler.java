package server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


class RequestHandler implements Runnable
{
    public RequestHandler(Socket socket) {
        this.socket = socket;
    }
    private Socket socket;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    @Override
    /*
     * request:
     * getroot
     *      - returns root directory with it's content (RemoteDirectory)
     *  getfile PATH
     *      - return raw bytes from a file
     *  deletefile PATH
     *      - return OK|FAIL
     *  deletedir PATH
     *      - return OK|FAIL
     *  upfile PATH
     *  Object - RemoteFileInfo
     *  file bytes
     *      - return OK|FAIL
     *  updir PATH
     *      - return OK|FAIL
     */
    
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        while(true)
        {
            try {
                System.out.println("start reading");
                var line = (String)input.readObject();
                if (line == null)
                {
                    return;
                }
                System.out.println("read: " + line);
                if (line.equals("getroot"))
                {
                    handleGetRoot();
                }
                else if (line.startsWith("getfile "))
                {
                    handleGetFile(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("deletefile "))
                {
                    handleDeleteFile(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("deletedir "))
                {
                    handleDeleteDir(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("upfile "))
                {
                    handleUpFile(line.split(" ", 2)[1], input);
                }
            } catch (Exception e) {
                try {
                    input.reset();
                } catch (IOException e1) {
                    return;
                }
                e.printStackTrace();
            }
        }
    }
    
    boolean isInRoot(String path)
    {
        if (!path.startsWith(TdpServer.getRoot().path))
        {
            System.out.println("Invalid path, not in root");
            return false;
        }
        else
        {
            return true;
        }
    }
    void sendOk() throws IOException
    {
        sendMessage("OK");
    }
    void sendFail() throws IOException
    {
        sendMessage("FAIL");
    }
    void sendMessage(String m) throws IOException
    {
        output.writeObject(m);
        output.flush();
    }
    void handleGetRoot() throws IOException
    {
            output.writeObject(TdpServer.getRoot());
    }
    
    void handleGetFile(String path) throws IOException
    {
        var p = Paths.get(path);
        if (!Files.isRegularFile(p))
        {
            System.out.println("File not found");
            return;
        }
        TdpServer.sendFile(socket,  path, null);
    }
    
    void handleDeleteFile(String path) throws IOException
    {
        if (!isInRoot(path))
        {
            System.out.println("file delete error: wrong path");
            System.out.println(path);
            sendFail();
            return;
        }
        var p = Paths.get(path);
        if (!Files.isRegularFile(p))
        {
            System.out.println("file delete error: not a file");
            System.out.println(path);
            sendFail();
            return;
        }
        Files.delete(p);
        TdpServer.syncFileList();
        System.out.println("file deleted ");
        System.out.println(path);
        sendOk();
    }
    
    void handleDeleteDir(String path) throws IOException
    {
        if (!isInRoot(path))
        {
            System.out.println("dir delete error: wrong path");
            System.out.println(path);
            sendFail();
            return;
        }
        var p = Paths.get(path);
        if (!Files.isDirectory(p))
        {
            System.out.println("dir delete error: not a dir");
            System.out.println(path);
            sendFail();
            return;
        }
        if (Files.list(p).count() != 0)
        {
            sendFail();
            return;
        }
        Files.delete(p);
        TdpServer.syncFileList();
        System.out.println("dir deleted ");
        System.out.println(path);
        sendOk();
    }

    void handleUpFile(String path, ObjectInputStream input) throws IOException, ClassNotFoundException
    {
        var file = (RemoteFileInfo)input.readObject();

        path = path.strip();
        var p = Paths.get(path);
        if (!isInRoot(path))
        {
            System.out.println("upfile error: wrong path");
            System.out.println(path);
            sendFail();
            return;
        }
        if (Files.exists(p))
        {
            System.out.println("upfile error: File already exist!");
            System.out.println(path);
            sendFail();
            return;
        }
        sendOk();
        TdpServer.receiveFile(socket, path, file.sizeBytes, null);
        var newFile = new RemoteFileInfo(path);
        if (Arrays.equals(file.md5digest, newFile.md5digest))
        {
            System.out.println("upfile success.");
            System.out.println(path);
            TdpServer.syncFileList();
            sendOk();
        }
        else
        {
            System.out.println("upfile failed wrong md5 sum.");
            System.out.println(path);
//            Files.delete(Paths.get(path));
            sendFail();
        }
    }
}
