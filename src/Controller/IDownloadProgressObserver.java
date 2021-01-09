package Controller;

public interface IDownloadProgressObserver {
    void updateProgress(int downloaded, int total);

}
