package se.gory_moon.player_mobs.utils;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TextureUtils {

    private static final Map<UUID, SkinType> SKIN_TYPE_CACHE = new Object2ObjectOpenHashMap<>();

    public static SkinType getPlayerSkinType(@Nullable GameProfile profile) {
        SkinType type = SkinType.DEFAULT;
        if (profile != null && profile.isComplete()) {
            if (SKIN_TYPE_CACHE.containsKey(profile.getId())) {
                type = SKIN_TYPE_CACHE.get(profile.getId());
            } else {
                Minecraft mc = Minecraft.getInstance();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = mc.getSkinManager().getInsecureSkinInformation(profile);
                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    String stringType = map.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
                    SKIN_TYPE_CACHE.put(profile.getId(), type = getType(stringType));
                } else {
                    type = getType(DefaultPlayerSkin.getSkinModelName(profile.getId()));
                }
            }
        }
        return type;
    }

    private static SkinType getType(@Nullable String stringType) {
        return "slim".equals(stringType) ? SkinType.SLIM : SkinType.DEFAULT;
    }

    public static ResourceLocation getPlayerSkin(PlayerMobEntity entity) {
        return getTexture(entity, MinecraftProfileTexture.Type.SKIN).orElse(DefaultPlayerSkin.getDefaultSkin());
    }

    public static Optional<ResourceLocation> getPlayerCape(PlayerMobEntity entity) {
        return getTexture(entity, MinecraftProfileTexture.Type.CAPE);
    }

    @SuppressWarnings("deprecation")
    private static Optional<ResourceLocation> getTexture(PlayerMobEntity entity, MinecraftProfileTexture.Type type) {
        if (entity.isTextureAvailable(type)) {
            return Optional.of(entity.getTexture(type));
        }

        GameProfile profile = entity.getProfile();
        if (profile != null && !profile.isComplete()) {
            return getDefault(profile, type);
        }

        if (profile != null && profile.getName() != null) {
            Minecraft mc = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = mc.getSkinManager().getInsecureSkinInformation(profile);
            if (map.containsKey(type)) {
                MinecraftProfileTexture profileTexture = map.get(type);
                // Use sha1 as that is how minecraft hashes and stores the skins
                String s = Hashing.sha1().hashUnencodedChars(profileTexture.getHash()).toString();
                ResourceLocation location = SkinManager.getTextureLocation(type, s);
                if (mc.textureManager.getTexture(location, MissingTextureAtlasSprite.getTexture()) != MissingTextureAtlasSprite.getTexture()) {
                    return Optional.of(location);
                } else {
                    RenderSystem.recordRenderCall(() -> {
                        mc.getSkinManager().registerTexture(profileTexture, type, entity.getSkinCallback());
                    });
                }
            }

        }
        return getDefault(profile, type);
    }

    private static Optional<ResourceLocation> getDefault(@Nullable GameProfile profile, MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.CAPE || type == MinecraftProfileTexture.Type.ELYTRA) {
            return Optional.empty();
        } else {
            return Optional.of(profile != null && profile.isComplete() ? DefaultPlayerSkin.getDefaultSkin(profile.getId()) : DefaultPlayerSkin.getDefaultSkin());
        }
    }

    public enum SkinType {
        DEFAULT,
        SLIM
    }
}
