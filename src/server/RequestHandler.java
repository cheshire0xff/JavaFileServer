package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
