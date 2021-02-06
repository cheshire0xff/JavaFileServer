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
    public RequestHandler(Socket socket, String rootPath) {
        this.rootDirPath = rootPath;
        this.socket = socket;
    }
    private String rootDirPath;
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
                else if (line.startsWith("getfile"))
                {
                    handleGetFile(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("deletefile"))
                {
                    handleDeleteFile(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("deletedir"))
                {
                    handleDeleteDir(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("upfile"))
                {
                    handleUpFile(line.split(" ", 2)[1], input);
                }
                else if (line.startsWith("updir"))
                {
                    handleUpDir(line.split(" ", 2)[1]);
                }
                else if (line.startsWith("deletedir"))
                {
                    handleDeleteDir(line.split(" ", 2)[1]);
                }
                else {
                	System.out.println("No codepath for line="+line);
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
        var p = Paths.get(rootDirPath, path);
        if (!Files.isRegularFile(p))
        {
            System.out.println("File not found");
            sendFail();
            return;
        }
        sendOk();
        TdpServer.sendFile(socket, p.toString(), null);
    }
    
    void handleDeleteFile(String path) throws IOException
    {
        var p = Paths.get(rootDirPath, path);
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
        var dir = Paths.get(rootDirPath, path).toFile();
        boolean ok = false;
        if (!dir.isDirectory())
        {
            System.out.println("deletedir fail, not a dir.");
            System.out.println(dir.toString());
        }
        if (dir.listFiles().length != 0)
        {
            System.out.println("deletedir fail, dir not empty.");
            System.out.println(dir.toString());
        }
        if (dir.delete())
        {
            ok = true;
        }
        else
        {
            System.out.println("deletedir fail.");
            System.out.println(dir.toString());
        }

        if (ok)
        {
            System.out.println("deletedir success.");
            System.out.println(dir.toString());
            TdpServer.syncFileList();
            sendOk();
        }
        else
        {
            sendFail();
        }
    }

    void handleUpFile(String path, ObjectInputStream input) throws IOException, ClassNotFoundException
    {
        var file = (RemoteFileInfo)input.readObject();

        var p = Paths.get(rootDirPath, path);
        if (Files.exists(p))
        {
            System.out.println("upfile error: File already exist!");
            System.out.println(p.toString());
            sendFail();
            return;
        }
        sendOk();
        p = TdpServer.receiveFile(socket, p.toString(), file.sizeBytes, null);
        var newFile = new RemoteFileInfo(p);
        if (Arrays.equals(file.md5digest, newFile.md5digest))
        {
            System.out.println("upfile success.");
            System.out.println(p.toString());
            TdpServer.syncFileList();
            sendOk();
        }
        else
        {
            System.out.println("upfile failed wrong md5 sum.");
            System.out.println(p.toString());
//            Files.delete(Paths.get(path));
            sendFail();
        }
        
    }
    void handleUpDir(String path) throws IOException
    {
        var dir = Paths.get(rootDirPath, path).toFile();
        if (dir.mkdir())
        {
            System.out.println("updir success.");
            System.out.println(dir.toString());
            TdpServer.syncFileList();
            sendOk();
        }
        else
        {
            System.out.println("updir fail.");
            System.out.println(dir.toString());
            sendFail();
        }
    }
}
