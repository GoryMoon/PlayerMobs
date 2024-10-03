package se.gory_moon.player_mobs.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextureUtils {

    private static final Map<UUID, PlayerSkin.Model> SKIN_MODEL_CACHE = new Object2ObjectOpenHashMap<>();

    private static final Map<String, ResourceLocation> SKIN_RESOURCE_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<String, ResourceLocation> CAPE_RESOURCE_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<String, ResourceLocation> ELYTRA_RESOURCE_CACHE = new Object2ObjectOpenHashMap<>();
    public static final ResourceLocation HIDE_FEATURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "hide_feature");

    public static PlayerSkin.Model getPlayerSkinType(PlayerMobEntity entity) {
        var optionalProfile = entity.getProfile();
        var type = PlayerSkin.Model.SLIM;

        if (optionalProfile.map(ResolvableProfile::isResolved).orElse(false)) {
            var profile = optionalProfile.get();
            if (SKIN_MODEL_CACHE.containsKey(profile.id().get())) {
                type = SKIN_MODEL_CACHE.get(profile.id().get());
            } else {
                Minecraft mc = Minecraft.getInstance();
                var playerSkin = mc.getSkinManager().getInsecureSkin(profile.gameProfile());
                SKIN_MODEL_CACHE.put(profile.id().get(), type = playerSkin.model());
            }
        }

        return type;
    }

    public static ResourceLocation getPlayerSkin(PlayerMobEntity entity) {
        String lowerName = entity.getUsername().skinName().toLowerCase(Locale.ROOT);

        // Check for custom overridden skins
        ResourceLocation location = SKIN_RESOURCE_CACHE.get(lowerName);
        if (location != null) {
            return location;
        }

        return getTexture(entity).map(PlayerSkin::texture).orElse(DefaultPlayerSkin.getDefaultTexture());
    }

    public static Optional<ResourceLocation> getPlayerCape(PlayerMobEntity entity) {
        String lowerName = entity.getUsername().skinName().toLowerCase(Locale.ROOT);

        // Check for custom overridden or disabled capes
        ResourceLocation location = CAPE_RESOURCE_CACHE.get(lowerName);
        if (location != null) {
            return location.equals(HIDE_FEATURE) ? Optional.empty() : Optional.of(location);
        }

        return getTexture(entity).map(PlayerSkin::capeTexture);
    }

    public static Optional<ResourceLocation> getPlayerElytra(PlayerMobEntity entity) {
        String lowerName = entity.getUsername().skinName().toLowerCase(Locale.ROOT);

        // Check for custom elytra
        ResourceLocation location = ELYTRA_RESOURCE_CACHE.get(lowerName);
        if (location != null) {
            return Optional.of(location);
        }

        return getTexture(entity).map(PlayerSkin::elytraTexture);
    }

    private static Optional<PlayerSkin> getTexture(PlayerMobEntity entity) {
        var profile = entity.getProfile();

        if (profile.map(ResolvableProfile::isResolved).orElse(false) && profile.get().name().isPresent()) {
            Minecraft mc = Minecraft.getInstance();
            return Optional.of(mc.getSkinManager().getInsecureSkin(profile.get().gameProfile()));
        }
        return getDefault(profile.orElse(null));
    }

    private static Optional<PlayerSkin> getDefault(@Nullable ResolvableProfile profile) {
        return Optional.ofNullable(profile != null && profile.isResolved() ? DefaultPlayerSkin.get(profile.id().get()) : null);
    }

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        SKIN_RESOURCE_CACHE.clear();

        var skins = resourceManager
                .listResources("skins", resourceLocation -> resourceLocation.getNamespace().equals(Constants.MOD_ID) && resourceLocation.getPath().endsWith(".png"));
        Pattern skinPattern = Pattern.compile("skins/([a-z0-9_.-]*).png");

        for (ResourceLocation location : skins.keySet()) {
            Matcher matcher = skinPattern.matcher(location.getPath());
            if (matcher.find()) {
                String name = matcher.group(1);
                SKIN_RESOURCE_CACHE.put(name, location);
            }
        }

        parseHideableTexture(resourceManager, "capes", CAPE_RESOURCE_CACHE);
        parseHideableTexture(resourceManager, "elytra", ELYTRA_RESOURCE_CACHE);
    }

    private static void parseHideableTexture(ResourceManager resourceManager, String type, Map<String, ResourceLocation> cache) {
        cache.clear();
        Pattern pattern = Pattern.compile(type + "/([a-z0-9_.-]*).(png|txt)");

        var resources = resourceManager.listResources(type, resourceLocation -> resourceLocation.getNamespace().equals(Constants.MOD_ID) &&
                (resourceLocation.getPath().endsWith(".png") || resourceLocation.getPath().endsWith(".txt")));

        for (ResourceLocation location : resources.keySet()) {
            Matcher matcher = pattern.matcher(location.getPath());
            if (matcher.find()) {
                String name = matcher.group(1);
                boolean ignore = Objects.equals(matcher.group(2), "txt");
                cache.put(name, ignore ? HIDE_FEATURE : location);
            }
        }
    }

    public static ResourceManagerReloadListener resourceManagerReloadListener() {
        return TextureUtils::onResourceManagerReload;
    }
}
