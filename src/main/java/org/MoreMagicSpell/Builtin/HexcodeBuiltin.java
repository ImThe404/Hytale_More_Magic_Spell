package org.MoreMagicSpell.Builtin;

import org.MoreMagicSpell.Builtin.glyphs.PetrifyGlyph.PetrifyGlyph;
import org.MoreMagicSpell.Builtin.glyphs.StoneWallGlyph.StoneWallGlyph;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;

public class HexcodeBuiltin {
    public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static void Setup() {
        GlyphRegistry.register(new PetrifyGlyph());
        GlyphRegistry.register(new StoneWallGlyph());
        LOGGER.atInfo().log("Registered 2 Hexcode Builtin Glyphs");
    }
}
