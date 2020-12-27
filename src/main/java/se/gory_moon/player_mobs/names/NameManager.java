package se.gory_moon.player_mobs.names;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.player_mobs.Configs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NameManager {

    public static final NameManager INSTANCE = new NameManager();
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<String> remoteNames = new ObjectOpenHashSet<>();
    private final Set<String> usedNames = new ObjectOpenHashSet<>();
    private final List<String> namePool = new ObjectArrayList<>();

    private boolean firstSync = true;
    private int tickTime = 0;
    private int syncTime = 0;
    private CompletableFuture<Integer> syncFuture = null;
    private final Random rand = new Random();
    private boolean setup = false;

    private NameManager() {
    }

    public void init() {
        if (!setup) {
            MinecraftForge.EVENT_BUS.addListener(this::serverTick);
            setup = true;
            updateNameList();
        }
    }

    public String getRandomName() {
        String name = namePool.get(rand.nextInt(namePool.size()));
        useName(name);
        return name;
    }

    public void useName(String name) {
        namePool.remove(name);
        usedNames.add(name);
        if (namePool.size() <= 0) {
            updateNameList();
        }
    }

    private void updateNameList() {
        Set<String> allNames = new ObjectOpenHashSet<>(Configs.COMMON.mobNames.get());
        allNames.addAll(remoteNames);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (setup && Configs.COMMON.useWhitelist.get() && server != null) {
            allNames.addAll(Arrays.asList(server.getPlayerList().getWhitelistedPlayerNames()));
        }

        if (namePool.size() > 0) {
            allNames.removeAll(usedNames);
            allNames.removeAll(namePool);
        } else {
            usedNames.clear();
        }
        namePool.addAll(allNames);
    }

    // SubscribeEvent
    private void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            syncTime++;

            if (tickTime > 0 && syncTime >= tickTime || firstSync) {
                firstSync = false;
                reloadRemoteLinks();
            }
        }
    }

    public void configLoad() {
        tickTime = Configs.COMMON.nameLinksSyncTime.get() * 1200; // time * 60 seconds * 20 ticks
        updateNameList();
    }

    public CompletableFuture<Integer> reloadRemoteLinks() {
        if (syncFuture != null && !syncFuture.isDone())
            return null;

        syncFuture = CompletableFuture.supplyAsync(() -> {
            Set<String> nameList = new ObjectOpenHashSet<>();
            for (String link : Configs.COMMON.nameLinks.get()) {
                try {
                    URL url = new URL(link);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            nameList.add(line);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(String.format("Error fetching names from %s", link), e);
                }
            }

            int diff = nameList.size();
            ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(EffectiveSide.get());
            Runnable updateTask = () -> {
                this.remoteNames.clear();
                this.remoteNames.addAll(nameList);
                updateNameList();
            };
            if (!executor.isOnExecutionThread()) {
                executor.deferTask(updateTask);
            } else {
                updateTask.run();
            }
            return diff;
        }, Util.getServerExecutor());
        return syncFuture;
    }
}
