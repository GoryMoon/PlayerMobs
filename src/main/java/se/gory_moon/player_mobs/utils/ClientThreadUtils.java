package se.gory_moon.player_mobs.utils;

import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class ClientThreadUtils {

    public static BlockableEventLoop<? extends Runnable> getExecutor() {
        return LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
    }
}