package code.util;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.stslib.icons.AbstractCustomIcon;

import java.util.Collections;
import java.util.List;

import static code.ModFile.makeID;

public class TicketIcon extends AbstractCustomIcon {
    public static final String ID = "ticket";
    private static TicketIcon singleton;
    private static final Texture iconTex = TexLoader.getTexture("vexmechanicResources/images/ui/Cube_icon.png");

    public TicketIcon() {
        super(ID, iconTex);
    }

    public static TicketIcon get() {
        if (singleton == null) {
            singleton = new TicketIcon();
        }
        return singleton;
    }

    @Override
    public List<String> keywordLinks() {
        return Collections.singletonList(makeID("ticket"));
    }
}
