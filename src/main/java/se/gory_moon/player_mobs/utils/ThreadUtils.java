package se.gory_moon.player_mobs.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

public class ThreadUtils {

    /***
     * Tries to run on the current main thread if available, if not it's run on a worker thread.
     * Should only be unavailable when executed before the server start on a dedicated server
     * @param runnable The runnable task that should be run
     */
    public static void tryRunOnMain(Runnable runnable) {
        var executor = LogicalSidedProvider.WORKQUEUE.get(FMLLoader.getDist() == Dist.CLIENT ? LogicalSide.CLIENT : LogicalSide.SERVER);
        executor.submitAsync(runnable);
    }

}
