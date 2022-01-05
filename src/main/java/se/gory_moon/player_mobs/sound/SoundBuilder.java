package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;


public class SoundBuilder<P> extends AbstractBuilder<SoundEvent, SoundEvent, P, SoundBuilder<P>> {

    private final NonNullFunction<ResourceLocation, SoundEvent> factory;
    private final String modId;

    public SoundBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, String modId) {
        super(owner, parent, name, callback, SoundEvent.class);
        this.modId = modId;
        this.factory = SoundEvent::new;
    }

    @Override
    protected @NonnullType SoundEvent createEntry() {
        return factory.apply(new ResourceLocation(modId, getName()));
    }
}
