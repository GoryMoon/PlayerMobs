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

    public double prevChasingPosX;
    public double prevChasingPosY;
    public double prevChasingPosZ;
    public double chasingPosX;
    public double chasingPosY;
    public double chasingPosZ;

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);

    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.createKey(PlayerMobEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> NAME = EntityDataManager.createKey(PlayerMobEntity.class, DataSerializers.STRING);

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
        return LivingEntity.registerAttributes()
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 35D)
                .createMutableAttribute(Attributes.ATTACK_KNOCKBACK)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.5D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.24D);
    }

    private boolean targetTwin(LivingEntity livingEntity) {
        return Configs.COMMON.attackTwin.get() || !(livingEntity instanceof PlayerEntity && livingEntity.getName().getString().equals(getUsername().getDisplayName()));
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        if(Configs.COMMON.openDoors.get() && world.getDifficulty() == Configs.COMMON.openDoorsDifficulty.get())
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
    protected void registerData() {
        super.registerData();
        getDataManager().register(NAME, "");
        getDataManager().register(IS_CHILD, false);
    }

    @Override
    public void updateRidden() {
    super.updateRidden();
        if (getRidingEntity() instanceof CreatureEntity) {
            CreatureEntity creatureentity = (CreatureEntity)getRidingEntity();
            renderYawOffset = creatureentity.renderYawOffset;
        }
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        if (rand.nextFloat() < (difficulty.getDifficulty() == Difficulty.HARD ? 0.1F: 0.5F)) {
            int i = rand.nextInt(3);

            if (i <= 1) {
                ItemStack stack = ItemManager.INSTANCE.getRandomMainHand(rand);
                setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
                if (rand.nextFloat() > 0.5f) {
                    if (stack.getItem() instanceof ShootableItem) {
                        ArrayList<ResourceLocation> potions = new ArrayList<>(ForgeRegistries.POTION_TYPES.getKeys());
                        Potion potion = ForgeRegistries.POTION_TYPES.getValue(potions.get(rand.nextInt(potions.size())));
                        setItemStackToSlot(EquipmentSlotType.OFFHAND, PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW), potion));
                    } else {
                        if (difficulty.getDifficulty() == Difficulty.HARD) {
                            setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemManager.INSTANCE.getRandomOffHand(rand));
                            getAttribute(Attributes.MAX_HEALTH).applyPersistentModifier(new AttributeModifier("Shield Bonus", rand.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);
        if (!this.world.isRemote) {
            this.setCombatTask();
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (IS_CHILD.equals(key)) {
            this.recalculateSize();
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        if (this.isChild()) {
            this.experienceValue = (int)((float)this.experienceValue * 2.5F);
        }

        return super.getExperiencePoints(player);
    }

    @Override
    public float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return this.isChild() ? 0.93F: 1.62F;
    }

    @Override
    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public boolean isElytraFlying() {
        return false;
    }

    @Override
    public double getYOffset() {
        return this.isChild() ? 0.0D : -0.45D;
    }

    @Override
    public boolean canEquipItem(ItemStack stack) {
        return (stack.getItem() != Items.EGG || !this.isChild() || !this.isPassenger()) && super.canEquipItem(stack);
    }

    @Override
    public void tick() {
        super.tick();
        this.prevChasingPosX = this.chasingPosX;
        this.prevChasingPosY = this.chasingPosY;
        this.prevChasingPosZ = this.chasingPosZ;
        double x = this.getPosX() - this.chasingPosX;
        double y = this.getPosY() - this.chasingPosY;
        double z = this.getPosZ() - this.chasingPosZ;
        double maxCapeAngle = 10.0D;
        if (x > maxCapeAngle) {
            this.chasingPosX = this.getPosX();
            this.prevChasingPosX = this.chasingPosX;
        }

        if (z > maxCapeAngle) {
            this.chasingPosZ = this.getPosZ();
            this.prevChasingPosZ = this.chasingPosZ;
        }

        if (y > maxCapeAngle) {
            this.chasingPosY = this.getPosY();
            this.prevChasingPosY = this.chasingPosY;
        }

        if (x < -maxCapeAngle) {
            this.chasingPosX = this.getPosX();
            this.prevChasingPosX = this.chasingPosX;
        }

        if (z < -maxCapeAngle) {
            this.chasingPosZ = this.getPosZ();
            this.prevChasingPosZ = this.chasingPosZ;
        }

        if (y < -maxCapeAngle) {
            this.chasingPosY = this.getPosY();
            this.prevChasingPosY = this.chasingPosY;
        }

        this.chasingPosX += x * 0.25D;
        this.chasingPosZ += z * 0.25D;
        this.chasingPosY += y * 0.25D;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean result = super.attackEntityAsMob(entityIn);
        if (result)
            swingArm(Hand.MAIN_HAND);
        return result;
    }

    @Override
    public boolean isChild() {
        return getDataManager().get(IS_CHILD);
    }

    @Override
    public void setChild(boolean isChild) {
        super.setChild(isChild);
        this.getDataManager().set(IS_CHILD, isChild);
        if (this.world != null && !this.world.isRemote) {
            ModifiableAttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attribute.removeModifier(BABY_SPEED_BOOST);
            if (isChild) {
                attribute.applyNonPersistentModifier(BABY_SPEED_BOOST);
            }
        }
    }

    @Override
    protected void onChildSpawnFromEgg(PlayerEntity player, MobEntity child) {
        if (child instanceof PlayerMobEntity) {
            ((PlayerMobEntity) child).setUsername(getUsername());
        }
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT dataTag) {
        spawnData = super.onInitialSpawn(world, difficulty, reason, spawnData, dataTag);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);

        setUsername(NameManager.INSTANCE.getRandomName());
        this.setCombatTask();
        float additionalDifficulty = difficulty.getClampedAdditionalDifficulty();
        this.setCanPickUpLoot(this.rand.nextFloat() < Configs.COMMON.pickupItemsChance.get() * additionalDifficulty);

        double rangeBonus = rand.nextDouble() * 1.5 * additionalDifficulty;
        if(rangeBonus > 1.0)
            getAttribute(Attributes.FOLLOW_RANGE).applyPersistentModifier(new AttributeModifier("Range Bonus", rangeBonus,  AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(rand.nextFloat() < additionalDifficulty * 0.05F)
            getAttribute(Attributes.MAX_HEALTH).applyPersistentModifier(new AttributeModifier("Health Bonus", rand.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(rand.nextFloat() < additionalDifficulty * 0.15F)
            getAttribute(Attributes.ATTACK_DAMAGE).applyPersistentModifier(new AttributeModifier("Damage Bonus", rand.nextDouble() + 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(rand.nextFloat() < additionalDifficulty * 0.2F)
            getAttribute(Attributes.MOVEMENT_SPEED).applyPersistentModifier(new AttributeModifier("Speed Bonus", rand.nextDouble() * 2.0 * 0.24 + 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL));

        if(rand.nextDouble() < Configs.COMMON.babySpawnChance.get())
            setChild(true);

        if(rand.nextFloat() < additionalDifficulty * 0.1F)
            setBreakDoorsAItask(true);

        return spawnData;
    }

    public void setCombatTask() {
        if (world != null && !world.isRemote) {
            goalSelector.removeGoal(aiArrowAttack);

            ItemStack itemstack = getHeldItem(ProjectileHelper.getHandWith(this, Items.BOW));
            if (itemstack.getItem() instanceof BowItem) {
                aiArrowAttack.setAttackCooldown(world.getDifficulty() != Difficulty.HARD ? 20: 40);
                goalSelector.addGoal(2, aiArrowAttack);
            }
        }
    }

    public void setBreakDoorsAItask(boolean enabled) {
        canBreakDoor = enabled;
        ((GroundPathNavigator) getNavigator()).setBreakDoors(enabled);
        if (enabled) {
            goalSelector.addGoal(1, breakDoor);
        } else {
            goalSelector.removeGoal(breakDoor);
        }
    }

    @Override
    public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack itemstack = findAmmo(getHeldItem(ProjectileHelper.getHandWith(this, Items.BOW)));
        AbstractArrowEntity abstractarrowentity = ProjectileHelper.fireArrow(this, itemstack, distanceFactor);
        if (getHeldItemMainhand().getItem() instanceof BowItem)
            abstractarrowentity = ((BowItem) getHeldItemMainhand().getItem()).customArrow(abstractarrowentity);
        double x = target.getPosX() - getPosX();
        double y = target.getPosYHeight(0.3333333333333333D) - abstractarrowentity.getPosY();
        double z = target.getPosZ() - getPosZ();
        double d3 = MathHelper.sqrt(x * x + z * z);
        abstractarrowentity.shoot(x, y + d3 * 0.2F, z, 1.6F, 14 - world.getDifficulty().getId() * 4);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(abstractarrowentity);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        String username = getUsername().getCombinedNames();
        if (!StringUtils.isNullOrEmpty(username)) {
            compound.putString("Username", username);
        }
        compound.putBoolean("CanBreakDoors", canBreakDoor);
        compound.putBoolean("IsBaby", isChild());
        if (profile != null && profile.isComplete()) {
            compound.put("Profile", NBTUtil.writeGameProfile(new CompoundNBT(), profile));
        }
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        PlayerName playerName;
        String username = compound.getString("Username");
        if (!StringUtils.isNullOrEmpty(username)) {
            playerName = new PlayerName(username);
            NameManager.INSTANCE.useName(playerName);
        } else {
            playerName = NameManager.INSTANCE.getRandomName();
        }
        setUsername(playerName);
        setChild(compound.getBoolean("IsBaby"));
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
        return new PlayerName(getDataManager().get(NAME));
    }

    public void setUsername(String name) {
        this.setUsername(new PlayerName(name));
    }
    public void setUsername(PlayerName name) {
        PlayerName oldName = getUsername();
        getDataManager().set(NAME, name.getCombinedNames());
        setCustomName(new StringTextComponent(name.getDisplayName()));

        if("Herobrine".equals(name.getDisplayName())){
            getAttribute(Attributes.ATTACK_DAMAGE).applyPersistentModifier(new AttributeModifier("Herobrine Damage Bonus", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            getAttribute(Attributes.MOVEMENT_SPEED).applyPersistentModifier(new AttributeModifier("Herobrine Speed Bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
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
