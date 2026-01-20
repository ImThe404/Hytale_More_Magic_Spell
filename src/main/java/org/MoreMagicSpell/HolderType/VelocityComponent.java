package org.MoreMagicSpell.HolderType;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class VelocityComponent implements Component<EntityStore> {
    private double vx;
    private double vy;
    private double vz;

    public VelocityComponent() {
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
    }

    public VelocityComponent(double vx, double vy, double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVelocity(double vx, double vy, double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    @Override
    public Component<EntityStore> clone() {
        return new VelocityComponent(vx, vy, vz);
    }
    
}
