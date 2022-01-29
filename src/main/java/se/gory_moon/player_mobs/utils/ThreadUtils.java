package se.gory_moon.player_mobs.utils;

import net.minecraft.Util;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ThreadUtils {

    /***
     * Tries to run on the current main thread if available, if not it's run on a worker thread.
     * Should only be unavailable when executed before the server start on a dedicated server
     * @param runnable The runnable task that should be run
     */
    public static void tryRunOnMain(Runnable runnable) {
        BlockableEventLoop<? extends Runnable> executor = DistExecutor.safeRunForDist(
                () -> ClientThreadUtils::getExecutor,
                () -> ServerLifecycleHooks::getCurrentServer);
        if (executor != null) {
            executor.submitAsync(runnable);
        } else {
            Util.backgroundExecutor().execute(runnable);
        }
    }

}
