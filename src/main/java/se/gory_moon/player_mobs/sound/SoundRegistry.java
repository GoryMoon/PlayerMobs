package se.gory_moon.player_mobs.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import se.gory_moon.player_mobs.Constants;

import java.util.function.Supplier;

import static se.gory_moon.player_mobs.Constants.*;

public class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);

    public static final Supplier<SoundEvent> PLAYER_MOB_AMBIENT = SOUNDS.register(SOUND_PLAYER_MOB_AMBIENT, SoundEvent::createVariableRangeEvent);
    public static final Supplier<SoundEvent> PLAYER_MOB_HURT = SOUNDS.register(SOUND_PLAYER_MOB_HURT, SoundEvent::createVariableRangeEvent);
    public static final Supplier<SoundEvent> PLAYER_MOB_DEATH = SOUNDS.register(SOUND_PLAYER_MOB_DEATH, SoundEvent::createVariableRangeEvent);
}
