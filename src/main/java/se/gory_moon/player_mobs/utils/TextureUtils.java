package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.Map;
import java.util.UUID;

public class TextureUtils {

    private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation("textures/entity/alex.png");

    private static final Map<UUID, SkinType> SKIN_TYPE_CACHE = new Object2ObjectOpenHashMap<>();

    public static SkinType getPlayerSkinType(GameProfile profile) {
        SkinType type = SkinType.DEFAULT;
        if (profile != null && profile.isComplete()) {
            if (SKIN_TYPE_CACHE.containsKey(profile.getId())) {
                type = SKIN_TYPE_CACHE.get(profile.getId());
            } else {
                Minecraft mc = Minecraft.getInstance();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(profile);
                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    String stringType = map.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
                    SKIN_TYPE_CACHE.put(profile.getId(), "slim".equals(stringType) ? SkinType.SLIM: SkinType.DEFAULT);
                }
            }
        }
        return type;
    }

    public static ResourceLocation getPlayerSkin(PlayerMobEntity entity) {
        return getTexture(entity, MinecraftProfileTexture.Type.SKIN);
    }

    public static ResourceLocation getPlayerCape(PlayerMobEntity entity) {
        return getTexture(entity, MinecraftProfileTexture.Type.CAPE);
    }

    private static ResourceLocation getTexture(PlayerMobEntity entity, MinecraftProfileTexture.Type type) {
        if (entity.isTextureAvailable(type)) {
            return entity.getTexture(type);
        }

        GameProfile profile = entity.getProfile();
        if (!entity.isProfileReady()) {
            return getDefault(profile, type);
        }

        if (profile != null && profile.getName() != null) {
            Minecraft mc = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(profile);
            if (map.containsKey(type)) {
                return mc.getSkinManager().loadSkin(map.get(type), type, entity.getSkinCallback());
            }
        }
        return getDefault(profile, type);
    }

    private static ResourceLocation getDefault(GameProfile profile, MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.CAPE || type == MinecraftProfileTexture.Type.ELYTRA) {
            return null;
        } else {
            return getPlayerSkinType(profile) == SkinType.SLIM ? TEXTURE_ALEX: TEXTURE_STEVE;
        }
    }

    public enum SkinType {
        DEFAULT,
        SLIM
    }
}
