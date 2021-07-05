package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class DeathHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof PlayerEntity && entity.getEntityWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            DamageSource source = event.getSource();
            if (source instanceof EntityDamageSource) {
                Entity trueSource = source.getTrueSource();
                if (trueSource instanceof PlayerEntity) {
                    ItemStack stack = ((PlayerEntity) trueSource).getActiveItemStack();
                    int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
                    ItemStack drop = getDrop(entity, source, looting);
                    if (!drop.isEmpty()) {
                        ((PlayerEntity) entity).dropItem(drop, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrop(LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof PlayerEntity || entity instanceof PlayerMobEntity) {
            ItemStack drop = getDrop(entity, event.getSource(), event.getLootingLevel());
            if (!drop.isEmpty()) {
                event.getDrops().add(new ItemEntity(entity.getEntityWorld(), entity.getPosX(), entity.getPosY(), entity.getPosZ(), drop));
            }
        }
    }

    private static ItemStack getDrop(LivingEntity entity, DamageSource source, int looting) {
        if (entity.getEntityWorld().isRemote() || entity.getHealth() > 0) return ItemStack.EMPTY;
        if (entity.isChild()) return ItemStack.EMPTY;
        double baseChance = entity instanceof PlayerMobEntity ? Configs.COMMON.mobHeadDropChance.get(): Configs.COMMON.playerHeadDropChance.get();
        if (baseChance <= 0) return ItemStack.EMPTY;

        if (poweredCreeper(source) || randomDrop(entity.getEntityWorld().getRandom(), baseChance, looting)) {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            GameProfile gameprofile = entity instanceof PlayerMobEntity ?
                    ((PlayerMobEntity) entity).getProfile():
                    ((PlayerEntity) entity).getGameProfile();
            if (entity instanceof PlayerMobEntity) {
                PlayerMobEntity pmentity = (PlayerMobEntity) entity;
                String skinName = pmentity.getUsername().getSkinName();
                String displayName = pmentity.getUsername().getDisplayName();
                if (!skinName.equals(displayName)) {
                    stack.setDisplayName(new StringTextComponent(displayName + "'s Head"));
                }
            }
            stack.getOrCreateTag().put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean poweredCreeper(DamageSource source) {
        if (source.isExplosion() && source instanceof EntityDamageSource) {
            Entity entity = source.getTrueSource();
            if (entity instanceof CreeperEntity)
                return ((CreeperEntity) entity).isCharged();
        }
        return false;
    }

    private static boolean randomDrop(Random rand, double baseChance, int looting) {
        return rand.nextDouble() <= Math.max(0, baseChance * Math.max(looting + 1, 1));
    }
}
