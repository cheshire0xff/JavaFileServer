package server;

public interface IDownloadProgressObserver {
    void updateProgress(int downloaded, int total);

}
