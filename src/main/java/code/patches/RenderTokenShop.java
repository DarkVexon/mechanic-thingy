package code.patches;

import code.CharacterFile;
import code.mechanic.VexMechanic;
import code.util.Wiz;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.DrawPilePanel;

@SpirePatch(
        clz = DrawPilePanel.class,
        method = "render"
)
public class RenderTokenShop {
    public static void Postfix(DrawPilePanel __instance, SpriteBatch spriteBatch) {
        if (Wiz.isInCombat() && AbstractDungeon.player.chosenClass.equals(CharacterFile.Enums.THE_VEX_MECHANIC))
            VexMechanic.render(spriteBatch);
    }
}
