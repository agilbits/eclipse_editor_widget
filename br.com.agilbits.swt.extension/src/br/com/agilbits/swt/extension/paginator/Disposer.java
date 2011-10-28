package br.com.agilbits.swt.extension.paginator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Disposer {
    private static final int MAX_THREADS = 10;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(MAX_THREADS); 
    private List<? extends Disposable> toDispose;

    public Disposer(List<? extends Disposable> toDispose) {
        this.toDispose = toDispose;
    }

    public void start() {
        POOL.execute(new Runnable() {
            public void run() {
                for (Disposable disposable : toDispose)
                    disposable.dispose();
            }
        });
    }
}
