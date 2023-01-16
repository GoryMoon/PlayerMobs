package se.gory_moon.player_mobs.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.sound.SoundRegistry;
import se.gory_moon.player_mobs.utils.ItemManager;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.utils.PlayerName;
import se.gory_moon.player_mobs.utils.ProfileUpdater;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerMobEntity extends Monster implements RangedAttackMob, CrossbowAttackMob {

    @Nullable
    private GameProfile profile;
    @Nullable
    private ResourceLocation skin;
    @Nullable
    private ResourceLocation cape;
    private boolean skinAvailable;
    private boolean capeAvailable;

    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);

    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(PlayerMobEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean canBreakDoors;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, (difficulty) -> difficulty == Difficulty.HARD);
    private final RangedBowAttackGoal<PlayerMobEntity> bowAttackGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
    private final RangedCrossbowAttackGoal<PlayerMobEntity> crossbowAttackGoal = new RangedCrossbowAttackGoal<>(this, 1.0D, 15.0F);

    public PlayerMobEntity(Level worldIn) {
        this(EntityRegistry.PLAYER_MOB_ENTITY.get(), worldIn);
    }

    public PlayerMobEntity(EntityType<? extends Monster> type, Level worldIn) {
        super(type, worldIn);
        this.setCombatTask();
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 35D)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_DAMAGE, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof Player && livingEntity.getName().getString().equals(getUsername().getDisplayName()));
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        if (Configs.COMMON.openDoors.get() && level.getDifficulty().getId() >= Configs.COMMON.openDoorsDifficulty.get().getId())
            goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombifiedPiglin.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::targetTwin));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(NAME, "");
        getEntityData().define(IS_CHILD, false);
        getEntityData().define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (getVehicle() instanceof PathfinderMob mob) {
            yBodyRot = mob.yBodyRot;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(pDifficulty);
        boolean force = Configs.COMMON.forceSpawnItem.get();
        if (force || random.nextFloat() < (this.level.getDifficulty() == Difficulty.HARD ? 0.5F: 0.1F)) {
            var stack = ItemManager.INSTANCE.getRandomMainHand(random);
            setItemSlot(EquipmentSlot.MAINHAND, stack);

            if (this.level.getDifficulty().getId() >= Configs.COMMON.offhandDifficultyLimit.get().getId() && random.nextDouble() > Configs.COMMON.offhandSpawnChance.get()) {
                if (stack.getItem() instanceof ProjectileWeaponItem && Configs.COMMON.allowTippedArrows.get()) {
                    var potions = new ArrayList<>(ForgeRegistries.POTIONS.getKeys());
                    potions.removeAll(Configs.COMMON.tippedArrowBlocklist);
                    if (potions.size() > 0) {
                        var potion = ForgeRegistries.POTIONS.getValue(potions.get(random.nextInt(potions.size())));
                        setItemSlot(EquipmentSlot.OFFHAND, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
                    }
                } else {
                    setItemSlot(EquipmentSlot.OFFHAND, ItemManager.INSTANCE.getRandomOffHand(random));
                    getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Shield Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (!this.level.isClientSide) {
            this.setCombatTask();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (IS_CHILD.equals(key)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    protected int getExperienceReward(Player player) {
        if (this.isBaby()) {
            this.xpReward = (int) ((float) this.xpReward * 2.5F);
        }

        return super.getExperienceReward(player);
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return this.isBaby() ? 0.93F : 1.62F;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return false;
    }

    @Override
    public boolean isFallFlying() {
        return false;
    }

    @Override
    public double getMyRidingOffset() {
        return this.isBaby() ? 0.0D : -0.45D;
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return (stack.getItem() != Items.EGG || !this.isBaby() || !this.isPassenger()) && super.canHoldItem(stack);
    }

    @Override
    public void tick() {
        super.tick();
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double x = this.getX() - this.xCloak;
        double y = this.getY() - this.yCloak;
        double z = this.getZ() - this.zCloak;
        double maxCapeAngle = 10.0D;
        if (x > maxCapeAngle) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (z > maxCapeAngle) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (y > maxCapeAngle) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        if (x < -maxCapeAngle) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (z < -maxCapeAngle) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (y < -maxCapeAngle) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        this.xCloak += x * 0.25D;
        this.zCloak += z * 0.25D;
        this.yCloak += y * 0.25D;
    }

    @Override
    public boolean doHurtTarget(Entity entityIn) {
        boolean result = super.doHurtTarget(entityIn);
        if (result)
            swing(InteractionHand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isBaby() {
        return getEntityData().get(IS_CHILD);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setBaby(boolean isChild) {
        super.setBaby(isChild);
        this.getEntityData().set(IS_CHILD, isChild);
        if (!this.level.isClientSide) {
            AttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(BABY_SPEED_BOOST);
            if (isChild) {
                attribute.addTransientModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player player, Mob child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        this.populateDefaultEquipmentSlots(difficulty);
        this.populateDefaultEquipmentEnchantments(difficulty);

        if (!hasUsername())
            setUsername(NameManager.INSTANCE.getRandomName());

        this.setCombatTask();
        float additionalDifficulty = difficulty.getSpecialMultiplier();
        setCanPickUpLoot(this.random.nextFloat() < Configs.COMMON.pickupItemsChance.get() * additionalDifficulty);
        setCanBreakDoors(random.nextFloat() < additionalDifficulty * 0.1F);

        double rangeBonus = random.nextDouble() * 1.5 * additionalDifficulty;
        if (rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Range Bonus", rangeBonus, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (random.nextFloat() < additionalDifficulty * 0.05F)
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Health Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (random.nextFloat() < additionalDifficulty * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Damage Bonus", random.nextDouble() + 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (random.nextFloat() < additionalDifficulty * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Speed Bonus", random.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if (random.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setBaby(true);

        return spawnData;
    }

    public void setCombatTask() {
        if (!level.isClientSide) {
            goalSelector.removeGoal(bowAttackGoal);
            goalSelector.removeGoal(crossbowAttackGoal);

            ItemStack itemstack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
            if (itemstack.getItem() instanceof CrossbowItem) {
                goalSelector.addGoal(2, crossbowAttackGoal);
            } else if (itemstack.getItem() instanceof BowItem) {
                bowAttackGoal.setMinAttackInterval(level.getDifficulty() != Difficulty.HARD ? 20 : 40);
                goalSelector.addGoal(2, bowAttackGoal);
            }
        }
    }

    public void setCanBreakDoors(boolean enabled) {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            if (canBreakDoors != enabled) {
                canBreakDoors = enabled;
                ((GroundPathNavigation) getNavigation()).setCanOpenDoors(enabled);
                if (enabled)
                    goalSelector.addGoal(1, breakDoorGoal);
                else
                    goalSelector.removeGoal(breakDoorGoal);
            }
        } else if (canBreakDoors) {
            this.goalSelector.removeGoal(breakDoorGoal);
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

    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbow, Projectile projectile, float angle) {
        this.shootCrossbowProjectile(this, target, projectile, angle, 1.6F);
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean pIsCharging) {
        this.entityData.set(IS_CHARGING_CROSSBOW, pIsCharging);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        ItemStack weaponStack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, this::canFireProjectileWeapon));
        if (weaponStack.getItem() instanceof CrossbowItem) {
            this.performCrossbowAttack(this, 1.6F);
        } else {
            ItemStack itemstack = getProjectile(weaponStack);
            AbstractArrow mobArrow = ProjectileUtil.getMobArrow(this, itemstack, pDistanceFactor);
            if (getMainHandItem().getItem() instanceof BowItem)
                mobArrow = ((BowItem) getMainHandItem().getItem()).customArrow(mobArrow);
            double x = pTarget.getX() - getX();
            double y = pTarget.getY(1D / 3D) - mobArrow.getY();
            double z = pTarget.getZ() - getZ();
            double d3 = Math.sqrt(x * x + z * z);
            mobArrow.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - level.getDifficulty().getId() * 4);
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
            this.level.addFreshEntity(mobArrow);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (getCustomName() != null && getCustomName().getString().isEmpty())
            compound.remove("CustomName");

        String username = getUsername().getCombinedNames();
        if (!StringUtil.isNullOrEmpty(username))
            compound.putString("Username", username);

        compound.putBoolean("CanBreakDoors", canBreakDoors);
        compound.putBoolean("IsBaby", isBaby());
        if (profile != null && profile.isComplete())
            compound.put("Profile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String username = compound.getString("Username");
        if (!StringUtil.isNullOrEmpty(username)) {
            setUsername(username);
        } else {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        setBaby(compound.getBoolean("IsBaby"));
        setCanBreakDoors(compound.getBoolean("CanBreakDoors"));

        if (compound.contains("Profile", Tag.TAG_COMPOUND)) {
            profile = NbtUtils.readGameProfile(compound.getCompound("Profile"));
        }

        setCombatTask();
    }

    @Override
    public Component getCustomName() {
        Component customName = super.getCustomName();
        String displayName = getUsername().getDisplayName();
        return customName != null && !customName.getString().isEmpty() ? customName : !StringUtil.isNullOrEmpty(displayName) ? new TextComponent(displayName) : null;
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName() || !StringUtil.isNullOrEmpty(getUsername().getDisplayName());
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
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundRegistry.PLAYER_MOB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.PLAYER_MOB_DEATH.get();
    }

    @Nullable
    public GameProfile getProfile() {
        if (profile == null && hasUsername()) {
            profile = new GameProfile(null, getUsername().getSkinName());
            ProfileUpdater.updateProfile(this);
        }
        return profile;
    }

    public void setProfile(@Nullable GameProfile profile) {
        this.profile = profile;
    }

    public boolean hasUsername() {
        return !StringUtil.isNullOrEmpty(getEntityData().get(NAME));
    }

    public PlayerName getUsername() {
        if (!hasUsername() && !level.isClientSide()) {
            setUsername(NameManager.INSTANCE.getRandomName());
        }
        return new PlayerName(getEntityData().get(NAME));
    }

    public void setUsername(String username) {
        PlayerName playerName = new PlayerName(username);
        if (playerName.noDisplayName()) {
            Optional<PlayerName> name = NameManager.INSTANCE.findName(username);
            if (name.isPresent())
                playerName = name.get();
        }
        NameManager.INSTANCE.useName(playerName);
        this.setUsername(playerName);
    }

    public void setUsername(PlayerName name) {
        PlayerName oldName = hasUsername() ? getUsername(): null;
        getEntityData().set(NAME, name.getCombinedNames());

        if ("Herobrine".equals(name.getDisplayName())) {
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Herobrine Damage Bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Herobrine Speed Bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (!Objects.equals(oldName, name)) {
            setProfile(null);
            getProfile();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public SkinManager.SkinTextureCallback getSkinCallback() {
        return (type, location, profileTexture) -> {
            switch (type) {
                case SKIN -> {
                    skin = location;
                    skinAvailable = true;
                }
                case CAPE -> {
                    cape = location;
                    capeAvailable = true;
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTextureAvailable(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skinAvailable;
        return capeAvailable;
    }

    @SuppressWarnings("ConstantConditions")
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(MinecraftProfileTexture.Type type) {
        if (type == MinecraftProfileTexture.Type.SKIN)
            return skin;
        return cape;
    }
}
