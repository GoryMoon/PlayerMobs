package se.gory_moon.player_mobs.entity;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.sound.SoundRegistry;
import se.gory_moon.player_mobs.utils.ItemManager;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.utils.PlayerName;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.world.level.block.entity.SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR;

public class PlayerMobEntity extends Monster implements RangedAttackMob, CrossbowAttackMob {

    private static final Logger LOGGER = LogUtils.getLogger();

    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;

    private static final String TAG_PROFILE = "Profile";
    private static final String TAG_CUSTOM_NAME = "CustomName";
    private static final String TAG_USERNAME = "Username";
    private static final String TAG_IS_BABY = "IsBaby";
    private static final String TAG_CAN_BREAK_DOORS = "CanBreakDoors";

    private static final ResourceLocation SPEED_MODIFIER_BABY_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "baby");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
            SPEED_MODIFIER_BABY_ID, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final ResourceLocation SHIELD_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.shield");
    private static final ResourceLocation RANGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.range");
    private static final ResourceLocation HEALTH_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.health");
    private static final ResourceLocation DAMAGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.damage");
    private static final ResourceLocation SPEED_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.speed");
    private static final ResourceLocation HEROBRINE_DAMAGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.damage.herobrine");
    private static final ResourceLocation HEROBRINE_SPEED_BONUS_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bonus.speed.herobrine");

    private static final EntityDimensions BABY_DIMENSIONS = EntityRegistry.PLAYER_MOB_ENTITY.get().getDimensions().scale(0.5F).withEyeHeight(0.93F);

    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_NAME_ID = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<ResolvableProfile>> DATA_PROFILE_ID = SynchedEntityData.defineId(PlayerMobEntity.class, EntityRegistry.RESOLVABLE_PROFILE_SERIALIZER.get());
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_CROSSBOW_ID = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean canBreakDoors;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, (difficulty) -> difficulty == Difficulty.HARD);
    private final RangedBowAttackGoal<PlayerMobEntity> bowAttackGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final RangedCrossbowAttackGoal<PlayerMobEntity> crossbowAttackGoal = new RangedCrossbowAttackGoal<>(this, 1.0D, 15.0F);

    public PlayerMobEntity(EntityType<? extends PlayerMobEntity> type, Level pLevel) {
        super(type, pLevel);
        setCombatTask();
    }

    public PlayerMobEntity(Level worldIn) {
        this(EntityRegistry.PLAYER_MOB_ENTITY.get(), worldIn);
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 35D)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_DAMAGE, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof Player && livingEntity.getName().getString().equals(getUsername().displayName()));
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        addBehaviourGoals();
    }

    private void addBehaviourGoals() {
        if (canOpenDoor()) {
            goalSelector.addGoal(1, new OpenDoorGoal(this, true));
            ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        }

        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombifiedPiglin.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::targetTwin));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    private boolean canOpenDoor() {
        return Configs.COMMON.openDoors.get() && level().getDifficulty().getId() >= Configs.COMMON.openDoorsDifficulty.get().getId();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_NAME_ID, "");
        pBuilder.define(DATA_BABY_ID, false);
        pBuilder.define(DATA_PROFILE_ID, Optional.empty());
        pBuilder.define(DATA_CHARGING_CROSSBOW_ID, false);
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (getVehicle() instanceof PathfinderMob mob) {
            yBodyRot = mob.yBodyRot;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(pRandom, pDifficulty);
        boolean force = Configs.COMMON.forceSpawnItem.get();
        if (force || pRandom.nextFloat() < (level().getDifficulty() == Difficulty.HARD ? 0.5F : 0.1F)) {
            var stack = ItemManager.INSTANCE.getRandomMainHand(pRandom);
            setItemSlot(EquipmentSlot.MAINHAND, stack);

            if (level().getDifficulty().getId() >= Configs.COMMON.offhandDifficultyLimit.get().getId() && pRandom.nextDouble() < Configs.COMMON.offhandSpawnChance.get()) {
                if (stack.getItem() instanceof ProjectileWeaponItem && Configs.COMMON.allowTippedArrows.get()) {
                    var potions = new ArrayList<>(BuiltInRegistries.POTION.keySet());
                    potions.removeAll(Configs.COMMON.tippedArrowBlocklist);
                    if (!potions.isEmpty()) {
                        var potion = BuiltInRegistries.POTION.getHolder(potions.get(pRandom.nextInt(potions.size())));
                        potion.ifPresent(potionReference -> setItemSlot(EquipmentSlot.OFFHAND, PotionContents.createItemStack(Items.TIPPED_ARROW, potionReference)));
                    }
                } else {
                    ItemStack randomOffHand = ItemManager.INSTANCE.getRandomOffHand(pRandom);
                    setItemSlot(EquipmentSlot.OFFHAND, randomOffHand);
                    if (randomOffHand.getItem() instanceof ShieldItem)
                        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH))
                                .addPermanentModifier(new AttributeModifier(SHIELD_BONUS_ID, pRandom.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (!level().isClientSide) {
            setCombatTask();
        }
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (DATA_BABY_ID.equals(key)) {
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
    }

    @Override
    protected int getBaseExperienceReward() {
        if (isBaby()) {
            xpReward = (int) ((float) xpReward * 2.5F);
        }

        return super.getBaseExperienceReward();
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pPose) {
        return isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pPose);
    }

    @Override
    public boolean isFallFlying() {
        return false;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return (stack.getItem() != Items.EGG || !isBaby() || !isPassenger()) && super.canHoldItem(stack);
    }

    @Override
    public void tick() {
        super.tick();
        xCloakO = xCloak;
        yCloakO = yCloak;
        zCloakO = zCloak;
        double x = getX() - xCloak;
        double y = getY() - yCloak;
        double z = getZ() - zCloak;
        double maxCapeAngle = 10.0D;
        if (x > maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z > maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y > maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        if (x < -maxCapeAngle) {
            xCloak = getX();
            xCloakO = xCloak;
        }

        if (z < -maxCapeAngle) {
            zCloak = getZ();
            zCloakO = zCloak;
        }

        if (y < -maxCapeAngle) {
            yCloak = getY();
            yCloakO = yCloak;
        }

        xCloak += x * 0.25D;
        zCloak += z * 0.25D;
        yCloak += y * 0.25D;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entityIn) {
        boolean result = super.doHurtTarget(entityIn);
        if (result)
            swing(InteractionHand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isBaby() {
        return entityData.get(DATA_BABY_ID);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setBaby(boolean isChild) {
        super.setBaby(isChild);
        entityData.set(DATA_BABY_ID, isChild);
        if (!level().isClientSide) {
            AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(SPEED_MODIFIER_BABY);
            if (isChild) {
                attribute.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player player, Mob child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, @NotNull MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
        RandomSource randomSource = pLevel.getRandom();
        populateDefaultEquipmentSlots(randomSource, pDifficulty);
        populateDefaultEquipmentEnchantments(pLevel, randomSource, pDifficulty);

        if (!hasUsername())
            setUsername(NameManager.INSTANCE.getRandomName());

        setCombatTask();
        float specialMultiplier = pDifficulty.getSpecialMultiplier();
        setCanPickUpLoot(randomSource.nextFloat() < Configs.COMMON.pickupItemsChance.get() * specialMultiplier);
        setCanBreakDoors(randomSource.nextFloat() < specialMultiplier * 0.1F);

        double rangeBonus = randomSource.nextDouble() * 1.5 * specialMultiplier;
        if (rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE)
                    .addPermanentModifier(new AttributeModifier(RANGE_BONUS_ID, rangeBonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.05F)
            getAttribute(Attributes.MAX_HEALTH)
                    .addPermanentModifier(new AttributeModifier(HEALTH_BONUS_ID, randomSource.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE)
                    .addPermanentModifier(new AttributeModifier(DAMAGE_BONUS_ID, randomSource.nextDouble() + 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (randomSource.nextFloat() < specialMultiplier * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED)
                    .addPermanentModifier(new AttributeModifier(SPEED_BONUS_ID, randomSource.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        if (randomSource.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setBaby(true);

        return pSpawnData;
    }

    public void setCombatTask() {
        if (!level().isClientSide) {
            goalSelector.removeGoal(bowAttackGoal);
            goalSelector.removeGoal(crossbowAttackGoal);

            ItemStack itemstack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
            if (itemstack.getItem() instanceof CrossbowItem) {
                goalSelector.addGoal(2, crossbowAttackGoal);
            } else if (itemstack.getItem() instanceof BowItem) {
                bowAttackGoal.setMinAttackInterval(level().getDifficulty() != Difficulty.HARD ? 20 : 40);
                goalSelector.addGoal(2, bowAttackGoal);
            }
        }
    }

    public void setCanBreakDoors(boolean enabled) {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            if (canBreakDoors != enabled) {
                canBreakDoors = enabled;
                ((GroundPathNavigation) getNavigation()).setCanOpenDoors(enabled || canOpenDoor());
                if (enabled)
                    goalSelector.addGoal(1, breakDoorGoal);
                else
                    goalSelector.removeGoal(breakDoorGoal);
            }
        } else if (canBreakDoors) {
            goalSelector.removeGoal(breakDoorGoal);
            canBreakDoors = false;
        }
    }

    public boolean canFireProjectileWeapon(Item item) {
        return item instanceof ProjectileWeaponItem weaponItem && canFireProjectileWeapon(weaponItem);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem || item instanceof CrossbowItem;
    }

    public boolean isChargingCrossbow() {
        return entityData.get(DATA_CHARGING_CROSSBOW_ID);
    }

    @Override
    public void setChargingCrossbow(boolean pIsCharging) {
        entityData.set(DATA_CHARGING_CROSSBOW_ID, pIsCharging);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        noActionTime = 0;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity pTarget, float pDistanceFactor) {
        ItemStack weapon = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
        if (weapon.getItem() instanceof CrossbowItem) {
            performCrossbowAttack(this, 1.6F);
        } else {
            ItemStack projectile = getProjectile(weapon);

            AbstractArrow mobArrow = ProjectileUtil.getMobArrow(this, projectile, 1.6F, weapon);
            if (getMainHandItem().getItem() instanceof ProjectileWeaponItem weaponItem)
                mobArrow = weaponItem.customArrow(mobArrow, projectile, weapon);

            double x = pTarget.getX() - getX();
            double y = pTarget.getY(1D / 3D) - mobArrow.getY();
            double z = pTarget.getZ() - getZ();
            double d3 = Math.sqrt(x * x + z * z);

            mobArrow.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - level().getDifficulty().getId() * 4);
            playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
            level().addFreshEntity(mobArrow);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getCustomName() != null && getCustomName().getString().isEmpty())
            compound.remove(TAG_CUSTOM_NAME);

        String username = getUsername().getCombinedNames();
        if (!StringUtil.isNullOrEmpty(username))
            compound.putString(TAG_USERNAME, username);

        compound.putBoolean(TAG_CAN_BREAK_DOORS, canBreakDoors);
        compound.putBoolean(TAG_IS_BABY, isBaby());
        getProfile().ifPresent(profile -> compound.put(TAG_PROFILE, ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).getOrThrow()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_PROFILE)) {
            ResolvableProfile.CODEC
                    .parse(NbtOps.INSTANCE, compound.getCompound(TAG_PROFILE))
                    .resultOrPartial(s -> LOGGER.error("Failed to load profile from player mob: {}", s))
                    .ifPresent(this::setProfile);
        }

        String username = compound.getString(TAG_USERNAME);
        if (!StringUtil.isNullOrEmpty(username)) {
            setUsername(username);
        } else {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        setBaby(compound.getBoolean(TAG_IS_BABY));
        setCanBreakDoors(compound.getBoolean(TAG_CAN_BREAK_DOORS));

        setCombatTask();
    }

    @Override
    public Component getCustomName() {
        Component customName = super.getCustomName();
        String displayName = getUsername().displayName();
        return customName != null && !customName.getString().isEmpty() ? customName : !StringUtil.isNullOrEmpty(displayName) ? Component.literal(displayName) : null;
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName() || !StringUtil.isNullOrEmpty(getUsername().displayName());
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.PLAYER_MOB_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.PLAYER_MOB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.PLAYER_MOB_DEATH.get();
    }

    public Optional<ResolvableProfile> getProfile() {
        return entityData.get(DATA_PROFILE_ID);
    }

    private void updateProfile(ResolvableProfile profile) {
        if (profile != null && !profile.isResolved()) {
            profile.resolve().thenAcceptAsync(resolvableProfile -> {
                entityData.set(DATA_PROFILE_ID, Optional.of(resolvableProfile));
            }, CHECKED_MAIN_THREAD_EXECUTOR);
        }
    }

    public void setProfile(ResolvableProfile profile) {
        entityData.set(DATA_PROFILE_ID, Optional.of(profile));
        updateProfile(profile);
    }

    public boolean hasUsername() {
        return !StringUtil.isNullOrEmpty(entityData.get(DATA_NAME_ID));
    }

    public PlayerName getUsername() {
        if (!hasUsername() && !level().isClientSide()) {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        return PlayerName.create(entityData.get(DATA_NAME_ID));
    }

    public void setUsername(String username) {
        PlayerName playerName = PlayerName.create(username);
        if (playerName.noDisplayName()) {
            Optional<PlayerName> name = NameManager.INSTANCE.findName(username);
            if (name.isPresent())
                playerName = name.get();
        }
        NameManager.INSTANCE.useName(playerName);
        setUsername(playerName);
    }

    public void setUsername(PlayerName name) {
        PlayerName oldName = hasUsername() ? getUsername() : null;
        entityData.set(DATA_NAME_ID, name.getCombinedNames());

        if ("Herobrine".equals(name.displayName())) {
            Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE))
                    .addPermanentModifier(new AttributeModifier(HEROBRINE_DAMAGE_BONUS_ID, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED))
                    .addPermanentModifier(new AttributeModifier(HEROBRINE_SPEED_BONUS_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (!Objects.equals(oldName, name)) {
            setProfile(new ResolvableProfile(Optional.of(name.skinName()), Optional.empty(), new PropertyMap()));
        }
    }
}
