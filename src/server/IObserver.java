package server;

public interface IObserver {
    /**
     * @param downloaded
     * @param total
     */
    void updateProgress(long downloaded, long total);
}
