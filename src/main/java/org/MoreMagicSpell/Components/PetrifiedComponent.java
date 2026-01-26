package org.MoreMagicSpell.Components;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.MoreMagicSpell.MoreMagicSpell;

public class PetrifiedComponent implements Component<EntityStore> {

    // Saving necessary data for petrification effect
    public static final BuilderCodec<PetrifiedComponent> CODEC =
        BuilderCodec.builder(PetrifiedComponent.class, PetrifiedComponent::new)
            .append(new KeyedCodec<Long>("EndTime", Codec.LONG),
                (c, v) -> c.endTime = v, c -> c.endTime)
            .add()
            .append(new KeyedCodec<Double>("XPos", Codec.DOUBLE),
                (c, v) -> c.XPos = v, c -> c.XPos)
            .add()
            .append(new KeyedCodec<Double>("YPos", Codec.DOUBLE),
                (c, v) -> c.YPos = v, c -> c.YPos)
            .add()
            .append(new KeyedCodec<Double>("ZPos", Codec.DOUBLE),
                (c, v) -> c.ZPos = v, c -> c.ZPos)
            .add()
            .append(new KeyedCodec<Float>("XRot", Codec.FLOAT),
                (c, v) -> c.XRot = v, c -> c.XRot)
            .add()
            .append(new KeyedCodec<Float>("YRot", Codec.FLOAT),
                (c, v) -> c.YRot = v, c -> c.YRot)
            .add()
            .append(new KeyedCodec<Float>("ZRot", Codec.FLOAT),
                (c, v) -> c.ZRot = v, c -> c.ZRot)
            .add()
            .build();

    // Serialized fields
    private long endTime;
    private double XPos, YPos, ZPos;
    private float XRot, YRot, ZRot;
    // Transient fields not to be serialized
    private transient Model originalModel;
    private transient String[] savedAnimations;

    public PetrifiedComponent() {}

    public PetrifiedComponent(long durationMs, String[] savedAnimations, Model originalModel, double XPos, double YPos, double ZPos, float XRot, float YRot, float ZRot) {
        this.endTime = System.currentTimeMillis() + durationMs;
        this.savedAnimations = savedAnimations;
        this.originalModel = originalModel;
        this.XPos = XPos;
        this.YPos = YPos;
        this.ZPos = ZPos;
        this.XRot = XRot;
        this.YRot = YRot;
        this.ZRot = ZRot;
    }

    @Nonnull
    public static ComponentType<EntityStore, PetrifiedComponent> getComponentType() {
        return MoreMagicSpell.get().getPetrifiedComponentType();
    }

    // Getters and other
    public long getEndTime() { return endTime; }
    public String[] getSavedAnimations() { return savedAnimations; }
    public Model getOriginalModel() { return originalModel; }
    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }
    public double getXPos() { return XPos; }
    public double getYPos() { return YPos; }
    public double getZPos() { return ZPos; }
    public float getXRot() { return XRot; }
    public float getYRot() { return YRot; }
    public float getZRot() { return ZRot; }

    @Override
    public Component<EntityStore> clone() {
        PetrifiedComponent copy = new PetrifiedComponent(endTime - System.currentTimeMillis(), savedAnimations, originalModel, XPos, YPos, ZPos, XRot, YRot, ZRot);
        return copy;
    }
}