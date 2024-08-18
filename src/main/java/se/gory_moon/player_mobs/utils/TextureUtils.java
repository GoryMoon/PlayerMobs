package se.gory_moon.player_mobs.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TextureUtils {

    private static final Map<UUID, PlayerSkin.Model> SKIN_MODEL_CACHE = new Object2ObjectOpenHashMap<>();

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
        return getTexture(entity).map(PlayerSkin::texture).orElse(DefaultPlayerSkin.getDefaultTexture());
    }

    public static Optional<ResourceLocation> getPlayerCape(PlayerMobEntity entity) {
        return getTexture(entity).map(PlayerSkin::capeTexture);
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
}
