package org.MoreMagicSpell.Builtin.glyphs.StoneWallGlyph;

import org.MoreMagicSpell.Spells.StoneWallSpell;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.state.execution.HexExecuter;
import com.riprod.hexcode.core.state.execution.component.HexContext;
import com.riprod.hexcode.utils.HexVarUtil;

public class StoneWallGlyph implements GlyphHandler {

    public static final String ID = "StoneWall";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var target = glyph.readSlot(StoneWallSlots.TARGET, hexContext);
        if (target == null) {
            return;
        }
        EntityVar entityVar = HexVarUtil.resolveEntityVar(target, hexContext);
        if (entityVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be an Entity");
            return;
        }

        Ref<EntityStore> originRef = entityVar.getRef(hexContext.getAccessor());
        if (originRef == null || !originRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is invalid");
            return;
        }

        StoneWallSpell.CastSpell(hexContext.getAccessor(), originRef);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public String getId() {
        return ID;
    }

}
