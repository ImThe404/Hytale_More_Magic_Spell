package org.MoreMagicSpell.Systems;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.MoreMagicSpell.Components.PetrifiedComponent;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
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
        // Restore model texture
        //LOGGER.atInfo().log("TEXTURE TO RESTORE: " + petrified.getOriginalModel());
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
        commandBuffer.replaceComponent(
        ref,
        ActiveAnimationComponent.getComponentType(),
        new ActiveAnimationComponent()
        );
        ActiveAnimationComponent animcomp = commandBuffer.getComponent(ref, ActiveAnimationComponent.getComponentType());
        AnimationSlot tmp = AnimationSlot.Movement;
        animcomp.setPlayingAnimation(tmp, null);

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
        // Replace Texture to use StoneWall Texture but keep the same model
        ModelComponent targetModel = store.getComponent(ref, ModelComponent.getComponentType());
        Model stoneModel = new Model(
            targetModel.getModel().getModelAssetId(),
            targetModel.getModel().getScale(),
            targetModel.getModel().getRandomAttachmentIds(),
            targetModel.getModel().getAttachments(),
            targetModel.getModel().getBoundingBox(),
            targetModel.getModel().getModel(),
            "NPC/Textures/Petrified.png",  // Only change this line to use stone texture
            targetModel.getModel().getGradientSet(),
            targetModel.getModel().getGradientId(),
            targetModel.getModel().getEyeHeight(),
            targetModel.getModel().getCrouchOffset(),
            Map.of(), // empty animation set map to prevent animation glitches
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
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(this.petrifiedComponentType);
  }
  
}
