package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.LangKeys;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class DeathHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Player && entity.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            DamageSource source = event.getSource();
            if (source instanceof EntityDamageSource) {
                Entity trueSource = source.getEntity();
                if (trueSource instanceof Player) {
                    ItemStack stack = ((Player) trueSource).getUseItem();
                    int looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
                    ItemStack drop = getDrop(entity, source, looting);
                    if (!drop.isEmpty()) {
                        ((Player) entity).drop(drop, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrop(LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Player || entity instanceof PlayerMobEntity) {
            ItemStack drop = getDrop(entity, event.getSource(), event.getLootingLevel());
            if (!drop.isEmpty()) {
                event.getDrops().add(new ItemEntity(entity.getCommandSenderWorld(), entity.getX(), entity.getY(), entity.getZ(), drop));
            }
        }
    }

    private static ItemStack getDrop(LivingEntity entity, DamageSource source, int looting) {
        if (entity.getCommandSenderWorld().isClientSide() || entity.getHealth() > 0)
            return ItemStack.EMPTY;
        if (entity.isBaby())
            return ItemStack.EMPTY;
        double baseChance = entity instanceof PlayerMobEntity ? Configs.COMMON.mobHeadDropChance.get(): Configs.COMMON.playerHeadDropChance.get();
        if (baseChance <= 0)
            return ItemStack.EMPTY;

        if (poweredCreeper(source) || randomDrop(entity.getCommandSenderWorld().getRandom(), baseChance, looting)) {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            GameProfile profile = entity instanceof PlayerMobEntity ?
                    ((PlayerMobEntity) entity).getProfile():
                    ((Player) entity).getGameProfile();
            if (entity instanceof PlayerMobEntity playerMob) {
                String skinName = playerMob.getUsername().getSkinName();
                String displayName = playerMob.getUsername().getDisplayName();
                if (!skinName.equals(displayName)) {
                    stack.setHoverName(new TranslatableComponent("block.minecraft.player_head.named", displayName));
                }
            }
            if (profile != null)
                stack.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), profile));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean poweredCreeper(DamageSource source) {
        if (source.isExplosion() && source instanceof EntityDamageSource) {
            Entity entity = source.getEntity();
            if (entity instanceof Creeper)
                return ((Creeper) entity).isPowered();
        }
        return false;
    }

    private static boolean randomDrop(Random rand, double baseChance, int looting) {
        return rand.nextDouble() <= Math.max(0, baseChance * Math.max(looting + 1, 1));
    }
}
