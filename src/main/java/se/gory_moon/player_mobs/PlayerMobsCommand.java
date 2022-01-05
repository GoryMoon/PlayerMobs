package se.gory_moon.player_mobs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.NameManager;

public class PlayerMobsCommand {

    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(new TranslatableComponent(LangKeys.COMMANDS_SPAWN_FAILED.key()));
    private static final SimpleCommandExceptionType DUPLICATE_UUID = new SimpleCommandExceptionType(new TranslatableComponent(LangKeys.COMMANDS_SPAWN_UUID.key()));
    private static final SimpleCommandExceptionType INVALID_POS = new SimpleCommandExceptionType(new TranslatableComponent(LangKeys.COMMANDS_SPAWN_INVALID_POS.key()));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal("playermobs")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("reload").executes(context -> {
                            context.getSource().sendSuccess(new TranslatableComponent(LangKeys.COMMANDS_RELOAD_START.key()), true);
                            NameManager.INSTANCE.reloadRemoteLinks().thenAccept(change -> {
                                context.getSource().sendSuccess(new TranslatableComponent(LangKeys.COMMANDS_RELOAD_DONE.key(), change), true);
                            });
                            return 1;
                        })
                ).then(Commands.literal("spawn")
                        .then(Commands
                                .argument("username", StringArgumentType.word())
                                .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), context.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(context -> spawnPlayerMob(context.getSource(), StringArgumentType.getString(context, "username"), Vec3Argument.getVec3(context, "pos"))))))
        );
    }

    private static int spawnPlayerMob(CommandSourceStack source, String username, Vec3 pos) throws CommandSyntaxException {
        BlockPos blockpos = new BlockPos(pos);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POS.create();
        } else {
            PlayerMobEntity entity = EntityRegistry.PLAYER_MOB_ENTITY.create(source.getLevel());
            if (entity == null) {
                throw SUMMON_FAILED.create();
            } else {
                entity.moveTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());
                entity.finalizeSpawn(source.getLevel(), source.getLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
                entity.setUsername(username);

                if (!source.getLevel().tryAddFreshEntityWithPassengers(entity)) {
                    throw DUPLICATE_UUID.create();
                }
                source.sendSuccess(new TranslatableComponent(LangKeys.COMMANDS_SPAWN_SUCCESS.key(), entity.getDisplayName()), true);
                return 1;
            }
        }
    }

}
