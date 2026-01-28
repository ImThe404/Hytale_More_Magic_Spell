package org.MoreMagicSpell.Interactions;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Interaction that spawns a stone wall entity in front of the player when used.
 */
public class SpawnStoneWallInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<SpawnStoneWallInteraction> CODEC = BuilderCodec.builder(
            SpawnStoneWallInteraction.class, SpawnStoneWallInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final float WALL_SPAWN_DISTANCE = 5.0f;

    private List<Vector2i> SHAPE_FLAT;
    private List<Vector2i> SHAPE_CORNER;

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        
        // Get CommandBuffer
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();

        // Verfication Checks for CommandBuffer, Player, and ItemStack
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }
        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("Player is null");
            return;
        }
        ItemStack itemStack = interactionContext.getHeldItem();
        if (itemStack == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("ItemStack is null");
            return;
        }

        // Get Store and World
        World world = commandBuffer.getExternalData().getWorld();
        Store<EntityStore> store = commandBuffer.getExternalData().getStore();

        // Create Entity Holder to make a new Entity
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        // Getting Model Asset and creating Model
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("StoneWall"); // Model in JSON in ressources
        Model model = Model.createScaledModel(modelAsset, 1.0f);
        // Getting Information from Player
        TransformComponent playerTransform = store.getComponent(ref, TransformComponent.getComponentType());
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        float direction = headRotation.getRotation().getY(); // facing direction of player

        // Getting direction on 45 degree increments
        direction = (float) (Math.round(direction / (Math.PI / 4)) * (Math.PI / 4));

        // Calculate spawn position in front of player
        double offsetX = -Math.sin(direction) * WALL_SPAWN_DISTANCE;
        double offsetZ = -Math.cos(direction) * WALL_SPAWN_DISTANCE;
        Vector3d wallPosition = new Vector3d(playerTransform.getPosition());
        wallPosition.add(
            new Vector3d(
                offsetX,
                0,
                offsetZ
            )
        );
        wallPosition = new Vector3d(((int)wallPosition.x)+0.5, (int)wallPosition.y, ((int)wallPosition.z)+0.5); // Center wall on block grid
        Vector3f wallRotation = new Vector3f(0, direction, 0); // align wall to face player
        TransformComponent wallTransform = new TransformComponent(wallPosition, wallRotation); // Make TransformComponent for wall
        // Add Components to Entity Holder for the Stone Wall
        holder.addComponent(TransformComponent.getComponentType(), wallTransform);
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        BoundingBox box = new BoundingBox(model.getBoundingBox());
        holder.addComponent(BoundingBox.getComponentType(), box);
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        // Animation will be used when spawning the wall so we make a variable
        ActiveAnimationComponent activeAnimationComponent = new ActiveAnimationComponent();
        holder.addComponent(ActiveAnimationComponent.getComponentType(), activeAnimationComponent);

        // Create Collision Shape based on Wallshape
        this.CreateShape();
        List<Vector2i> Wallshape = this.getShape(direction);
        Vector3i centerOfWall = new Vector3i(
            (int) Math.floor(wallPosition.x),
            (int) Math.floor(wallPosition.y),
            (int) Math.floor(wallPosition.z)
        );
        player.sendMessage(Message.raw("Block List : " + Wallshape));
        for (Vector2i v : Wallshape) { // For each position in the wall shape
            for (int y_add = 0 ; y_add < 3 ; y_add++) { // Wall height of 3 blocks
                Vector3i blockPos = new Vector3i(
                    centerOfWall.x + v.x,
                    centerOfWall.y + y_add,
                    centerOfWall.z + v.y
                );
                world.setBlock(
                    blockPos.x,
                    blockPos.y,
                    blockPos.z,
                    "InvisibleBlock"); // Set block to invisible block for collision only
            }
        }
        
        // Interaction effect
        world.execute(() -> {   
            store.addEntity(holder, AddReason.SPAWN);
            activeAnimationComponent.setPlayingAnimation(AnimationSlot.Action, "Spawn");
        });


    }

    // Making Shapes for Collision Detection
    private void CreateShape() {
        //   █ █ █ █ █
        // █           █
        SHAPE_FLAT = new java.util.ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            SHAPE_FLAT.add(new Vector2i(i, 0));
        }
        SHAPE_FLAT.add(new Vector2i(-3,  1));
        SHAPE_FLAT.add(new Vector2i( 3,  1));

        //         █ █
        //       █   
        //     █   
        //   █   
        // █
        // █
        SHAPE_CORNER = new java.util.ArrayList<>();
        for (int i = 2; i <= 3; i++) {
            SHAPE_CORNER.add(new Vector2i(-2, i));
            SHAPE_CORNER.add(new Vector2i(i, -2));
        }
        for (int i = -1; i <= 1; i++) {
            SHAPE_CORNER.add(new Vector2i(i, -i));
        }
    }

    private List<Vector2i> getShape(float direction) {
        // Direction [-PI, PI]
        // Determine shape based on direction and return rotated shape
        // 0, 90, 180, 270 degree -> flat shape
        // 45, 135, 225, 315 degree -> corner shape
        int shapeIndex = (int) Math.round(direction / (Math.PI / 4)) % 8;
        // absolute value on shapeIndex
        if (shapeIndex < 0) shapeIndex += 8;
        List<Vector2i> result = new ArrayList<>();
        
        switch (shapeIndex) {
            // FLAT SHAPE
            case 0:
                for (Vector2i v : SHAPE_FLAT) result.add(new Vector2i(v.x,  v.y)); // North
                break;
            case 2:
                for (Vector2i v : SHAPE_FLAT) result.add(new Vector2i(v.y,  -v.x)); // West
                break;
            case 4:
                for (Vector2i v : SHAPE_FLAT) result.add(new Vector2i(-v.x, -v.y)); // South
                break;
            case 6:
                for (Vector2i v : SHAPE_FLAT) result.add(new Vector2i( -v.y, v.x)); // East
                break;

            // CORNER SHAPE
            case 1:
                for (Vector2i v : SHAPE_CORNER) result.add(new Vector2i(v.x,  v.y)); // North-West
                break;
            case 3:
                for (Vector2i v : SHAPE_CORNER) result.add(new Vector2i(v.y,  -v.x)); // South-West
                break;
            case 5:
                for (Vector2i v : SHAPE_CORNER) result.add(new Vector2i(-v.x, -v.y)); // South-East
                break;
            case 7:
                for (Vector2i v : SHAPE_CORNER) result.add(new Vector2i( -v.y, v.x)); // North-East
                break;
        }
        LOGGER.atInfo().log("Spawn Wall at index " + shapeIndex);
        return result;
    }
    
}
