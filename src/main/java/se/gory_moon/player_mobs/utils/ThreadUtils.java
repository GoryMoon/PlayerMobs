package se.gory_moon.player_mobs.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ThreadUtils {

    /***
     * Tries to run on the current main thread if available, if not it's run on a worker thread.
     * Should only be unavailable when executed before the server start on a dedicated server
     * @param runnable The runnable task that should be
     */
    public static void tryRunOnMain(Runnable runnable) {
        ThreadTaskExecutor<? extends Runnable> executor = DistExecutor.safeRunForDist(() -> Minecraft::getInstance, () -> ServerLifecycleHooks::getCurrentServer);
        if (executor != null) {
            executor.deferTask(runnable);
        } else {
            Util.getServerExecutor().execute(runnable);
        }
    }

}
