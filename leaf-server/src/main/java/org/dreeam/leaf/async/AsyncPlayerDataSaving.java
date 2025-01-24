package org.dreeam.leaf.async;

import net.minecraft.Util;
import org.dreeam.leaf.config.modules.async.AsyncPlayerDataSave;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AsyncPlayerDataSaving {

    private AsyncPlayerDataSaving() {
    }

    public static void saveAsync(Runnable runnable) {
        if (!AsyncPlayerDataSave.enabled) {
            runnable.run();
            return;
        }

        ExecutorService ioExecutor = Util.backgroundExecutor().service();

        CompletableFuture.runAsync(runnable, ioExecutor);
    }
}
