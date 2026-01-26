package org.MoreMagicSpell.Systems;

import javax.annotation.Nonnull;
import org.MoreMagicSpell.Components.PetrifiedComponent;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PetrifiedSystem extends EntityTickingSystem<EntityStore> {

  private final ComponentType<EntityStore, PetrifiedComponent> petrifiedComponentType;

  private static final HytaleLogger LOGGER = HytaleLogger.getLogger();

  public PetrifiedSystem(ComponentType<EntityStore, PetrifiedComponent> petrifiedComponentType) {
    this.petrifiedComponentType = petrifiedComponentType;
  }

  @Override
  public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    // Get PetrifiedComponent and Ref
    PetrifiedComponent petrified = archetypeChunk.getComponent(index, petrifiedComponentType);
    Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

    if (petrified.isExpired()) {
        // Restore animations
        if (petrified.getSavedAnimations() != null) {
            commandBuffer.replaceComponent(
            ref,
            ActiveAnimationComponent.getComponentType(),
            new ActiveAnimationComponent(petrified.getSavedAnimations()));
        }
        // Restore model texture and animations
        if (petrified.getOriginalModel() != null) {
            ModelComponent targetModel = store.getComponent(ref, ModelComponent.getComponentType());
            Model stoneModel = new Model(
            targetModel.getModel().getModelAssetId(),
            targetModel.getModel().getScale(),
            targetModel.getModel().getRandomAttachmentIds(),
            targetModel.getModel().getAttachments(),
            targetModel.getModel().getBoundingBox(),
            targetModel.getModel().getModel(),
            petrified.getOriginalModel().getTexture(),
            targetModel.getModel().getGradientSet(),
            targetModel.getModel().getGradientId(),
            targetModel.getModel().getEyeHeight(),
            targetModel.getModel().getCrouchOffset(),
            petrified.getOriginalModel().getAnimationSetMap(),
            targetModel.getModel().getCamera(),
            targetModel.getModel().getLight(),
            targetModel.getModel().getParticles(),
            targetModel.getModel().getTrails(),
            targetModel.getModel().getPhysicsValues(),
            targetModel.getModel().getDetailBoxes(),
            targetModel.getModel().getPhobia(),
            targetModel.getModel().getPhobiaModelAssetId()
        );
        commandBuffer.replaceComponent(
        ref,
        ModelComponent.getComponentType(),
        new ModelComponent(stoneModel));
        }
        // Remove PetrifiedComponent
        commandBuffer.removeComponent(ref, petrifiedComponentType);
        LOGGER.atInfo().log("Petrified effect expired for entity: " + ref.toString());
    } else {
        // Petrified effect ongoing - ensure entity remains petrified
        // Stop all animations every tick
        commandBuffer.replaceComponent(
        ref,
        ActiveAnimationComponent.getComponentType(),
        new ActiveAnimationComponent()
        );
        // Maintain position and rotation
        TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        transform.setPosition(new Vector3d(
            petrified.getXPos(),
            petrified.getYPos(),
            petrified.getZPos()
        ));
        transform.setRotation(new Vector3f(
            petrified.getXRot(),
            petrified.getYRot(),
            petrified.getZRot()
        ));
    }
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(this.petrifiedComponentType);
  }
  
}
