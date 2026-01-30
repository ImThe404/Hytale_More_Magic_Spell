package org.MoreMagicSpell.Components;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.MoreMagicSpell.MoreMagicSpell;
public class PetrifiedComponent implements Component<EntityStore> {

    // Saving necessary data for petrification effect
    public static final BuilderCodec<PetrifiedComponent> CODEC =
        BuilderCodec.builder(PetrifiedComponent.class, PetrifiedComponent::new)
            .append(new KeyedCodec<Long>("EndTime", Codec.LONG),
                (c, v) -> c.endTime = v, c -> c.endTime)
            .add()
            .build();

    // Serialized fields
    private long endTime;
    // Transient fields not to be serialized
    Holder<EntityStore> originalHolder;

    public PetrifiedComponent() {}

    public PetrifiedComponent(long petrifyDurationMs, Holder<EntityStore> originalHolder) {
        this.endTime = System.currentTimeMillis() + petrifyDurationMs;
        this.originalHolder = originalHolder;
    }

    @Nonnull
    public static ComponentType<EntityStore, PetrifiedComponent> getComponentType() {
        return MoreMagicSpell.get().getPetrifiedComponentType();
    }

    // Getters and other
    public long getEndTime() { return endTime; }
    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }
    public Holder<EntityStore> getOriginalHolder() {
        return originalHolder;
    }

    @Override
    public Component<EntityStore> clone() {
        PetrifiedComponent copy = new PetrifiedComponent(endTime - System.currentTimeMillis(), originalHolder);
        return copy;
    }
}