package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;


public class SoundBuilder<P> extends AbstractBuilder<SoundEvent, SoundEvent, P, SoundBuilder<P>> {

    private final NonNullFunction<ResourceLocation, SoundEvent> factory;
    private final String modid;

    public SoundBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, String modid) {
        super(owner, parent, name, callback, SoundEvent.class);
        this.modid = modid;
        this.factory = SoundEvent::new;
    }

    @Override
    protected @NonnullType SoundEvent createEntry() {
        return factory.apply(new ResourceLocation(modid, getName()));
    }
}
