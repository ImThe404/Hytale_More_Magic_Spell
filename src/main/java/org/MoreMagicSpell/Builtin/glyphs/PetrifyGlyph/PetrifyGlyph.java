package org.MoreMagicSpell.Builtin.glyphs.PetrifyGlyph;

import org.MoreMagicSpell.Spells.PetrifySpell;
import org.MoreMagicSpell.Spells.StoneWallSpell;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.state.execution.HexExecuter;
import com.riprod.hexcode.core.state.execution.component.HexContext;

public class PetrifyGlyph implements GlyphHandler {
    public static final String ID = "Petrify";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var target = glyph.readSlot(PetrifySlots.TARGET, hexContext);
        if (target == null) {
            return;
        }

        if (target instanceof EntityVar entityTarget) {
            PetrifySpell.CastSpell(hexContext.getAccessor(), entityTarget.getRef(hexContext.getAccessor()));
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public String getId() {
        return ID;
    }

}
