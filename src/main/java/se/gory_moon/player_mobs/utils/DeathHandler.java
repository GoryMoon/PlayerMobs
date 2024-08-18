package se.gory_moon.player_mobs.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.Optional;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class DeathHandler {

    /**
     * Drops a head if a player killed another player and KEEP_INVENTORY is enabled, as normal drop logic doesn't run
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player && entity.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            DamageSource source = event.getSource();
            Entity trueSource = source.getEntity();
            if (trueSource instanceof Player player) {
                ItemStack stack = player.getUseItem();
                Optional<Holder.Reference<Enchantment>> holder = entity.level().holder(Enchantments.LOOTING);
                int looting = holder.map(stack::getEnchantmentLevel).orElse(0);
                ItemStack drop = getDrop(entity, source, looting);
                if (!drop.isEmpty()) {
                    player.drop(drop, true);
                }
            }
        }
    }

    /**
     * Drops a head if a player or player mob is killed
     */
    @SubscribeEvent
    public static void onLivingDrop(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || entity instanceof PlayerMobEntity) {

            int lootingLevel = 0;

            Optional<Holder.Reference<Enchantment>> holder = entity.level().holder(Enchantments.LOOTING);
            if (event.getSource().getEntity() instanceof LivingEntity livingEntity && holder.isPresent()) {
                lootingLevel = EnchantmentHelper.getEnchantmentLevel(holder.get(), livingEntity);
            }

            ItemStack drop = getDrop(entity, event.getSource(), lootingLevel);
            if (!drop.isEmpty()) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), drop));
            }
        }
    }

    private static ItemStack getDrop(LivingEntity entity, DamageSource source, int looting) {
        if (entity.level().isClientSide() || entity.getHealth() > 0)
            return ItemStack.EMPTY;
        if (entity.isBaby())
            return ItemStack.EMPTY;

        double baseChance = entity instanceof PlayerMobEntity ? Configs.COMMON.mobHeadDropChance.get() : Configs.COMMON.playerHeadDropChance.get();
        if (baseChance <= 0)
            return ItemStack.EMPTY;

        if (poweredCreeper(source) || randomDrop(entity.level().getRandom(), baseChance, looting)) {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            GameProfile profile = entity instanceof PlayerMobEntity playerMobEntity ?
                    playerMobEntity.getProfile().map(ResolvableProfile::gameProfile).orElse(null) :
                    ((Player) entity).getGameProfile();

            if (entity instanceof PlayerMobEntity playerMob) {
                String skinName = playerMob.getUsername().skinName();
                Object displayName = playerMob.getUsername().displayName();
                Component customName = playerMob.getCustomName();
                if (customName != null)
                    displayName = customName;

                if (!skinName.equals(displayName)) {
                    stack.set(DataComponents.CUSTOM_NAME, Component.translatable("block.minecraft.player_head.named", displayName));
                }
            }
            if (profile != null)
                stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean poweredCreeper(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            Entity entity = source.getEntity();
            if (entity instanceof Creeper creeper)
                return creeper.isPowered();
        }
        return false;
    }

    private static boolean randomDrop(RandomSource rand, double baseChance, int looting) {
        return rand.nextDouble() <= Math.max(0, baseChance * Math.max(looting + 1, 1));
    }
}
