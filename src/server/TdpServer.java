package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class RequestHandler implements Runnable
{
    private static final int timeout = (int) Duration.ofSeconds(10).toMillis();
    public RequestHandler(Socket socket) {
        this.socket = socket;
    }
    public Socket socket;
    @Override
    /*
     * request:
     * getroot
     *      - returns root directory with it's content (RemoteDirectory)
     *  getfile PATH
     *      - return raw bytes from a file
     */
    public void run() {
    }
}

public class TdpServer {
    
    private final static int maxConnections = 10;

    public static void main(String[] args) {
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

}
