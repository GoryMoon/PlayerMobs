package se.gory_moon.playermobs.names;

import net.minecraft.tileentity.SkullTileEntity;
import se.gory_moon.playermobs.entity.PlayerMobEntity;

import java.util.ArrayDeque;
import java.util.Queue;

public class ProfileUpdater {

    private static final Queue<PlayerMobEntity> entities = new ArrayDeque<>();
    private static Thread thread;

    public static void updateProfile(PlayerMobEntity entity) {
        entities.add(entity);

        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new Thread(() -> {
                while (!entities.isEmpty()) {
                    PlayerMobEntity mob = entities.remove();
                    if (mob != null) {
                        mob.setProfile(SkullTileEntity.updateGameProfile(mob.getProfile()));
                    }
                }
            });
            thread.start();
        }
    }

}
