package org.MoreMagicSpell.HolderType;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HealthComponent implements Component<EntityStore> {

    public static final BuilderCodec<HealthComponent> CODEC =
        BuilderCodec.builder(HealthComponent.class, HealthComponent::new)
            .append(new KeyedCodec<>("MaxHealth", Codec.FLOAT),
                (c, v) -> c.maxHealth = v, c -> c.maxHealth)
            .add()
            .append(new KeyedCodec<>("CurrentHealth", Codec.FLOAT),
                (c, v) -> c.currentHealth = v, c -> c.currentHealth)
            .add()
            .build();

    private float maxHealth = 100f;
    private float currentHealth = 100f;

    public HealthComponent() {}

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public float getMaxHealth() { return maxHealth; }
    public float getCurrentHealth() { return currentHealth; }

    public void setCurrentHealth(float health) {
        this.currentHealth = Math.min(health, maxHealth);
    }

    public void damage(float amount) {
        this.currentHealth = Math.max(0, currentHealth - amount);
    }

    @Override
    public Component<EntityStore> clone() {
        HealthComponent copy = new HealthComponent(maxHealth);
        copy.currentHealth = this.currentHealth;
        return copy;
    }
}