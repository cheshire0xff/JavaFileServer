package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


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
     *  deletefile PATH
     *      - return OK|FAIL
     *  deletedir PATH
     *      - return OK|FAIL
     */
    
    public void run() {
        while(true)
        {
            try {
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
            } catch (Exception e) {
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
        var writer = new PrintWriter(socket.getOutputStream());
        writer.println(m);
        writer.flush();
    }
    void handleGetRoot() throws IOException
    {
            var objectStream = new ObjectOutputStream(socket.getOutputStream());
            objectStream.writeObject(TdpServer.getRoot());
    }
    
    void handleGetFile(String path) throws IOException
    {
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
            int chunk = TdpServer.downloadChunkSize; // max size
            if (size < chunk)
            {
                chunk = size;
            }
            size -= chunk;
            var buf = fileInput.readNBytes((int) chunk);
            socket.getOutputStream().write(buf);
            socket.getOutputStream().flush();
        }
        fileInput.close();
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
}
