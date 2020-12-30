package se.gory_moon.player_mobs.utils;

import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class ClientThreadUtils {

    public static ThreadTaskExecutor<? extends Runnable> getExecutor() {
        return LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
    }
}