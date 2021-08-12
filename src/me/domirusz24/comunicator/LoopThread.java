package me.domirusz24.comunicator;

public class LoopThread extends Thread {

    private final Runnable stop;

    @Override
    public void run() {
        while (true) {
            if (Main.running) {
                super.run();
            } else {
                stop.run();
                break;
            }
        }
    }

    public LoopThread(Runnable target, Runnable stop) {
        super(target);
        this.stop = stop;
    }

    public LoopThread(Runnable target) {
        super(target);
        this.stop = () -> {};
    }

    public Runnable getStop() {
        return stop;
    }
}
