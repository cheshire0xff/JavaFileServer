package ClientApi;

import server.IObserver;

public class Observer implements IObserver
{
    public Observer(String text)
    {
        this.text = text;
    }
    String text;
    @Override
    public void updateProgress(long downloaded, long total) {
        System.out.print(text + downloaded + "/" + total + " bytes\r");
        System.out.flush();
    }
    
}