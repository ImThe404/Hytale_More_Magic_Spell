package org.MoreMagicSpell.Builtin.glyphs.StoneWallGlyph;

import org.MoreMagicSpell.Spells.StoneWallSpell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class StoneWallGlyph implements GlyphHandler {

    public static final String ID = "StoneWall";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var target = glyph.readSlot(StoneWallSlots.TARGET, hexContext);
        if (target == null) {
            return;
        }
        var rotationVar = glyph.readSlot(StoneWallSlots.ROTATION, hexContext, target);
        var durationVar = glyph.readSlot(StoneWallSlots.DURATION, hexContext);
        var duration = durationVar.toScalar();

        if (duration == null) {
            duration = 8d;
        }

        var position = HexVarUtil.resolvePositionVar(target, hexContext);
        var rotation = HexVarUtil.resolveRotationVar(rotationVar, hexContext);

        StoneWallSpell.CastSpell(hexContext.getAccessor(), position.getValue(), rotation.getValue(),
                duration.intValue() * 1000);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public String getId() {
        return ID;
    }

}
