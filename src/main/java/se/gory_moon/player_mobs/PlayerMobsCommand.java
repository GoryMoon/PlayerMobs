package se.gory_moon.player_mobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.NameManager;

public class PlayerMobsCommand {

    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent(LangKeys.COMMANDS_SPAWN_FAILED.key()));
    private static final SimpleCommandExceptionType DUPLICATE_UUID = new SimpleCommandExceptionType(new TranslationTextComponent(LangKeys.COMMANDS_SPAWN_UUID.key()));
    private static final SimpleCommandExceptionType INVALID_POS = new SimpleCommandExceptionType(new TranslationTextComponent(LangKeys.COMMANDS_SPAWN_INVALID_POS.key()));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands
                .literal("playermobs")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.literal("reload").executes(context -> {
                            context.getSource().sendFeedback(new TranslationTextComponent(LangKeys.COMMANDS_RELOAD_START.key()), true);
                            NameManager.INSTANCE.reloadRemoteLinks().thenAccept(change -> {
                                context.getSource().sendFeedback(new TranslationTextComponent(LangKeys.COMMANDS_RELOAD_DONE.key(), change), true);
                            });
                            return 1;
                        })
                ).then(Commands.literal("spawn")
                        .then(Commands
                                .argument("username", StringArgumentType.word())
                                .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), context.getSource().getPos()))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"))))))
        );
    }

    private static int spawnPlayerMob(CommandSource source, String username, Vector3d pos) throws CommandSyntaxException {
        BlockPos blockpos = new BlockPos(pos);
        if (!World.isInvalidPosition(blockpos)) {
            throw INVALID_POS.create();
        } else {
            PlayerMobEntity entity = EntityRegistry.PLAYER_MOB_ENTITY.create(source.getWorld());
            if (entity == null) {
                throw SUMMON_FAILED.create();
            } else {
                entity.setLocationAndAngles(pos.x, pos.y, pos.z, entity.rotationYaw, entity.rotationPitch);
                entity.onInitialSpawn(source.getWorld(), source.getWorld().getDifficultyForLocation(entity.getPosition()), SpawnReason.COMMAND, null, null);
                entity.setUsername(username);

                if (!source.getWorld().addEntityAndUniquePassengers(entity)) {
                    throw DUPLICATE_UUID.create();
                }
                source.sendFeedback(new TranslationTextComponent(LangKeys.COMMANDS_SPAWN_SUCCESS.key(), entity.getDisplayName()), true);
                return 1;
            }
        }
    }

}
