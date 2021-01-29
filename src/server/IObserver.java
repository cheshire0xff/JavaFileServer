package server;

public interface IObserver {
    void updateProgress(long downloaded, long total);
}
