package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.Map;

public class TextureUtils {

    private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation("textures/entity/alex.png");

    public static String getPlayerSkinType(GameProfile profile) {
        String type = "default";
        if (profile != null && profile.getName() != null) {
            Minecraft mc = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = mc.getSkinManager().loadSkinFromCache(profile);
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN))
                type = map.get(MinecraftProfileTexture.Type.SKIN).getMetadata("model");
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
            return "slim".equals(getPlayerSkinType(profile)) ? TEXTURE_ALEX: TEXTURE_STEVE;
        }
    }
}
