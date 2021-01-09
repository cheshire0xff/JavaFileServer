package server;

public interface IObserver {
    void updateProgress(int downloaded, int total);
}
