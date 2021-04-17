package bkav.android.btalk;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//class utilities de chay cac tac vu khac main thread
public class BtalkExecutors {

    private static final Executor BG_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void runOnBGThread(Runnable runnable) {
        BG_EXECUTOR.execute(runnable);
    }

}
