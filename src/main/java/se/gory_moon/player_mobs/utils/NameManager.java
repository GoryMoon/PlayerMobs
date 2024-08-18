package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.player_mobs.Configs;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class NameManager {

    public static final NameManager INSTANCE = new NameManager();
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<PlayerName> allNames = ConcurrentHashMap.newKeySet();
    private final Set<PlayerName> remoteNames = ConcurrentHashMap.newKeySet();
    private final Set<PlayerName> usedNames = ConcurrentHashMap.newKeySet();
    private final Queue<PlayerName> namePool = new ConcurrentLinkedQueue<>();

    private boolean firstSync = true;
    private int tickTime = 0;
    private int syncTime = 0;
    @Nullable
    private CompletableFuture<Integer> syncFuture = null;
    private boolean setup = false;

    private NameManager() {
    }

    public void init() {
        if (!setup) {
            NeoForge.EVENT_BUS.addListener(this::serverTick);
            setup = true;
            updateNameList();
        }
    }

    public PlayerName getRandomName() {
        PlayerName name = namePool.poll();
        if (name == null)
            name = PlayerName.create("Gory_Moon");
        useName(name);
        return name;
    }

    public void useName(PlayerName name) {
        namePool.remove(name);
        usedNames.add(name);
        if (namePool.isEmpty()) {
            updateNameList();
        }
    }

    public Optional<PlayerName> findName(String name) {
        for (PlayerName playerName : allNames) {
            if (playerName.displayName().equalsIgnoreCase(name))
                return Optional.of(playerName);
        }
        return Optional.empty();
    }

    private void updateNameList() {
        Set<PlayerName> allNames = new ObjectOpenHashSet<>();
        for (String name : Configs.COMMON.mobNames.get()) {
            allNames.add(PlayerName.create(name));
        }
        allNames.addAll(remoteNames);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (setup && Configs.COMMON.useWhitelist.get() && server != null) {
            for (String name : server.getPlayerList().getWhiteListNames()) {
                allNames.add(PlayerName.create(name));
            }
        }

        allNames.removeIf(PlayerName::isInvalid);
        this.allNames.clear();
        this.allNames.addAll(allNames);

        if (!namePool.isEmpty()) {
            allNames.removeAll(usedNames);
            allNames.removeAll(namePool);
        } else {
            usedNames.clear();
        }
        ObjectArrayList<PlayerName> names = new ObjectArrayList<>(allNames);
        Collections.shuffle(names);
        namePool.addAll(names);
    }

    // SubscribeEvent
    private void serverTick(ServerTickEvent.Post event) {
        syncTime++;

        if (tickTime > 0 && syncTime >= tickTime || firstSync) {
            syncTime = 0;
            firstSync = false;
            reloadRemoteLinks(event.getServer());
        }
    }

    public void configLoad() {
        tickTime = Configs.COMMON.nameLinksSyncTime.get() * 1200; // time * 60 seconds * 20 ticks
        updateNameList();
    }

    public CompletableFuture<Integer> reloadRemoteLinks(MinecraftServer server) {
        if (syncFuture != null && !syncFuture.isDone())
            return CompletableFuture.completedFuture(0);

        syncFuture = CompletableFuture.supplyAsync(() -> {
            Set<PlayerName> nameList = new ObjectOpenHashSet<>();
            for (String link : Configs.COMMON.nameLinks.get()) {
                try {
                    URL url = URI.create(link).toURL();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            nameList.add(PlayerName.create(line));
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(String.format("Error fetching names from %s", link), e);
                }
            }

            int diff = nameList.size();

            ThreadUtils.tryRunOnMain(() -> {
                this.remoteNames.clear();
                this.remoteNames.addAll(nameList);
                updateNameList();
            });
            return diff;
        }, Util.backgroundExecutor());
        syncFuture.thenAccept(i -> updateUserProfileCache(server));

        return syncFuture;
    }

    private void updateUserProfileCache(MinecraftServer server) {
        GameProfileCache profileCache = server.getProfileCache();
        GameProfileRepository profileRepository = server.getProfileRepository();

        if (profileCache == null) return;

        ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile gameProfile) {
                profileCache.add(gameProfile);
                try {
                    Thread.sleep(2500);
                } catch (final InterruptedException ignored) {
                }
            }

            @Override
            public void onProfileLookupFailed(String username, Exception e) {
                if (e instanceof AuthenticationException authException && authException.getMessage().contains("429"))
                    LOGGER.warn("Could not lookup user entry for {}, probably because of rate-limit from mojang", username);
                if (e instanceof ProfileNotFoundException)
                    profileCache.add(UUIDUtil.createOfflineProfile(username));
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException ignored) {
                }
            }
        };

        while (true) {
            Set<String> names = profileCache.profilesByName.keySet();
            var nonCachedNames = allNames.stream()
                    .map(p -> p.skinName().toLowerCase(Locale.ROOT))
                    .collect(Collectors.partitioningBy(names::contains)).get(false)
                    .toArray(String[]::new);

            if (nonCachedNames.length == 0) return;

            if (server.usesAuthentication())
                profileRepository.findProfilesByNames(nonCachedNames, profilelookupcallback);
            else
                for (String name : nonCachedNames)
                    profileCache.add(UUIDUtil.createOfflineProfile(name));
        }
    }
}
