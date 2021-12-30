package se.gory_moon.player_mobs.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.utils.ProfileUpdater;
import se.gory_moon.player_mobs.sound.SoundRegistry;
import se.gory_moon.player_mobs.utils.ItemManager;
import se.gory_moon.player_mobs.utils.PlayerName;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PlayerMobEntity extends MonsterEntity implements IRangedAttackMob {

    private GameProfile profile;
    private ResourceLocation skin;
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

    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.defineId(PlayerMobEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> NAME = EntityDataManager.defineId(PlayerMobEntity.class, DataSerializers.STRING);

    private boolean canBreakDoor;
    private final BreakDoorGoal breakDoor = new BreakDoorGoal(this, (difficulty) -> difficulty == Difficulty.HARD);
    private final RangedBowAttackGoal<PlayerMobEntity> aiArrowAttack = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);

    public PlayerMobEntity(World worldIn) {
        this(EntityRegistry.PLAYER_MOB_ENTITY.get(), worldIn);
    }

    public PlayerMobEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
        this.setCombatTask();
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 35D)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_DAMAGE, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof PlayerEntity && livingEntity.getName().getString().equals(getUsername().getDisplayName()));
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        if(Configs.COMMON.openDoors.get() && level.getDifficulty() == Configs.COMMON.openDoorsDifficulty.get())
            goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(4, new RandomWalkingGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.addGoal(5, new LookRandomlyGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombifiedPiglinEntity.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::targetTwin));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(NAME, "");
        getEntityData().define(IS_CHILD, false);
    }

    @Override
    public void rideTick() {
    super.rideTick();
        if (getVehicle() instanceof CreatureEntity) {
            CreatureEntity creatureentity = (CreatureEntity)getVehicle();
            yBodyRot = creatureentity.yBodyRot;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(difficulty);
        if (random.nextFloat() < (difficulty.getDifficulty() == Difficulty.HARD ? 0.1F: 0.5F)) {
            int i = random.nextInt(3);

            if (i <= 1) {
                ItemStack stack = ItemManager.INSTANCE.getRandomMainHand(random);
                setItemSlot(EquipmentSlotType.MAINHAND, stack);
                if (random.nextFloat() > 0.5f) {
                    if (stack.getItem() instanceof ShootableItem) {
                        ArrayList<ResourceLocation> potions = new ArrayList<>(ForgeRegistries.POTION_TYPES.getKeys());
                        Potion potion = ForgeRegistries.POTION_TYPES.getValue(potions.get(random.nextInt(potions.size())));
                        setItemSlot(EquipmentSlotType.OFFHAND, PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
                    } else {
                        if (difficulty.getDifficulty() == Difficulty.HARD) {
                            setItemSlot(EquipmentSlotType.OFFHAND, ItemManager.INSTANCE.getRandomOffHand(random));
                            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Shield Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlotType slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (!this.level.isClientSide) {
            this.setCombatTask();
        }
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        if (IS_CHILD.equals(key)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    protected int getExperienceReward(PlayerEntity player) {
        if (this.isBaby()) {
            this.xpReward = (int)((float)this.xpReward * 2.5F);
        }

        return super.getExperienceReward(player);
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return this.isBaby() ? 0.93F: 1.62F;
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
            swing(Hand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isBaby() {
        return getEntityData().get(IS_CHILD);
    }

    @Override
    public void setBaby(boolean isChild) {
        super.setBaby(isChild);
        this.getEntityData().set(IS_CHILD, isChild);
        if (this.level != null && !this.level.isClientSide) {
            ModifiableAttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(BABY_SPEED_BOOST);
            if (isChild) {
                attribute.addTransientModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void onOffspringSpawnedFromEgg(PlayerEntity player, MobEntity child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT dataTag) {
        spawnData = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        this.populateDefaultEquipmentSlots(difficulty);
        this.populateDefaultEquipmentEnchantments(difficulty);

        setUsername(NameManager.INSTANCE.getRandomName());
        this.setCombatTask();
        float additionalDifficulty = difficulty.getSpecialMultiplier();
        this.setCanPickUpLoot(this.random.nextFloat() < Configs.COMMON.pickupItemsChance.get() * additionalDifficulty);

        double rangeBonus = random.nextDouble() * 1.5 * additionalDifficulty;
        if(rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Range Bonus", rangeBonus,  AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(random.nextFloat() < additionalDifficulty * 0.05F)
            getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Health Bonus", random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(random.nextFloat() < additionalDifficulty * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Damage Bonus", random.nextDouble() + 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(random.nextFloat() < additionalDifficulty * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Speed Bonus", random.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(random.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setBaby(true);

        if(random.nextFloat() < additionalDifficulty * 0.1F)
            setBreakDoorsAItask(true);

        return spawnData;
    }

    public void setCombatTask() {
        if (level != null && !level.isClientSide) {
            goalSelector.removeGoal(aiArrowAttack);

            ItemStack itemstack = getItemInHand(ProjectileHelper.getWeaponHoldingHand(this, Items.BOW));
            if (itemstack.getItem() instanceof BowItem) {
                aiArrowAttack.setMinAttackInterval(level.getDifficulty() != Difficulty.HARD ? 20: 40);
                goalSelector.addGoal(2, aiArrowAttack);
            }
        }
    }

    public void setBreakDoorsAItask(boolean enabled) {
        canBreakDoor = enabled;
        ((GroundPathNavigator) getNavigation()).setCanOpenDoors(enabled);
        if (enabled) {
            goalSelector.addGoal(1, breakDoor);
        } else {
            goalSelector.removeGoal(breakDoor);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack itemstack = getProjectile(getItemInHand(ProjectileHelper.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrowEntity abstractarrowentity = ProjectileHelper.getMobArrow(this, itemstack, distanceFactor);
        if (getMainHandItem().getItem() instanceof BowItem)
            abstractarrowentity = ((BowItem) getMainHandItem().getItem()).customArrow(abstractarrowentity);
        double x = target.getX() - getX();
        double y = target.getY(1D / 3D) - abstractarrowentity.getY();
        double z = target.getZ() - getZ();
        double d3 = MathHelper.sqrt(x * x + z * z);
        abstractarrowentity.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - level.getDifficulty().getId() * 4);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(abstractarrowentity);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        String username = getUsername().getCombinedNames();
        if (!StringUtils.isNullOrEmpty(username)) {
            compound.putString("Username", username);
        }
        compound.putBoolean("CanBreakDoors", canBreakDoor);
        compound.putBoolean("IsBaby", isBaby());
        if (profile != null && profile.isComplete()) {
            compound.put("Profile", NBTUtil.writeGameProfile(new CompoundNBT(), profile));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        PlayerName playerName;
        String username = compound.getString("Username");
        if (!StringUtils.isNullOrEmpty(username)) {
            playerName = new PlayerName(username);
            NameManager.INSTANCE.useName(playerName);
        } else {
            playerName = NameManager.INSTANCE.getRandomName();
        }
        setUsername(playerName);
        setBaby(compound.getBoolean("IsBaby"));
        setBreakDoorsAItask(compound.getBoolean("CanBreakDoors"));

        if (compound.contains("Profile", Constants.NBT.TAG_COMPOUND)) {
            profile = NBTUtil.readGameProfile(compound.getCompound("Profile"));
        }

        setCombatTask();
    }

    @Override
    public ITextComponent getName() {
        ITextComponent customName = getCustomName();
        return customName != null ? customName: new StringTextComponent(getUsername().getDisplayName());
    }

    @Override
    public ITextComponent getDisplayName() {
        return getName();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.PLAYER_MOB_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundRegistry.PLAYER_MOB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.PLAYER_MOB_DEATH.get();
    }

    public GameProfile getProfile() {
        if (profile == null && getUsername() != null && !getUsername().getSkinName().isEmpty()) {
            profile = new GameProfile(null, getUsername().getSkinName());
            ProfileUpdater.updateProfile(this);
        }
        return profile;
    }

    public void setProfile(@Nullable GameProfile profile) {
        this.profile = profile;
    }

    public PlayerName getUsername() {
        return new PlayerName(getEntityData().get(NAME));
    }

    public void setUsername(String name) {
        this.setUsername(new PlayerName(name));
    }
    public void setUsername(PlayerName name) {
        PlayerName oldName = getUsername();
        getEntityData().set(NAME, name.getCombinedNames());
        setCustomName(new StringTextComponent(name.getDisplayName()));

        if("Herobrine".equals(name.getDisplayName())){
            getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Herobrine Damage Bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("Herobrine Speed Bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (!Objects.equals(oldName, name)) {
            setProfile(null);
            getProfile();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public SkinManager.ISkinAvailableCallback getSkinCallback() {
        return (type, location, profileTexture) -> {
            switch (type) {
                case SKIN:
                    skin = location;
                    skinAvailable = true;
                    break;
                case CAPE:
                    cape = location;
                    capeAvailable = true;
                    break;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTextureAvailable(MinecraftProfileTexture.Type type) {
        switch (type) {
            case SKIN:
                return skinAvailable;
            case CAPE:
            default:
                return capeAvailable;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(MinecraftProfileTexture.Type type) {
        switch (type) {
            case SKIN:
                return skin;
            case CAPE:
            default:
                return cape;
        }
    }
}
