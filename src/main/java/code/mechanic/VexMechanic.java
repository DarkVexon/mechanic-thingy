package code.mechanic;

import code.util.TexLoader;
import code.util.Wiz;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;

import java.util.ArrayList;

public class VexMechanic {

    private static FrameBuffer fbo;

    public static void init() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
    }

    public static ArrayList<VexMechanicBenefit> costOneBenefits = new ArrayList<>();
    public static ArrayList<VexMechanicBenefit> costTwoBenefits = new ArrayList<>();
    public static ArrayList<VexMechanicBenefit> costFiveBenefits = new ArrayList<>();

    static {
        costOneBenefits.add(new VexMechanicBenefit("Draw two cards.", () -> Wiz.atb(new DrawCardAction(2)), 1));
        costOneBenefits.add(new VexMechanicBenefit("Deal 5 damage to ALL enemies.", () -> Wiz.atb(new DamageAllEnemiesAction(AbstractDungeon.player, DamageInfo.createDamageMatrix(5, true), DamageInfo.DamageType.THORNS, AbstractGameAction.AttackEffect.FIRE)), 1));
    }

    public static VexMechanicBenefit cost1;
    public static VexMechanicBenefit cost2;
    public static VexMechanicBenefit cost5;

    public static void atStartOfCombat() {
        cost1 = Wiz.getRandomItem(costOneBenefits, AbstractDungeon.cardRandomRng);
        //cost2 = Wiz.getRandomItem(costTwoBenefits, AbstractDungeon.cardRandomRng);
        //cost5 = Wiz.getRandomItem(costFiveBenefits, AbstractDungeon.cardRandomRng);
    }

    private static final float POS_X = Settings.WIDTH / 2 - (140F * Settings.scale);
    private static final float POS_Y = Settings.HEIGHT / 3;

    private static Texture panelBG = TexLoader.getTexture("vexmechanicResources/images/ui/panelBg.png");
    private static Texture mask = TexLoader.getTexture("vexmechanicResources/images/ui/rowMask.png");


    public static void render(SpriteBatch sb) {
        sb.draw(panelBG, POS_X, POS_Y);

        sb.setColor(Color.WHITE.cpy());
        sb.end();
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glColorMask(true, true, true, true);
        sb.begin();

        sb.setColor(Color.WHITE.cpy());
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (VexMechanicBenefit b : costOneBenefits) {
            b.render(sb);
        }

        sb.setBlendFunction(0, GL20.GL_SRC_ALPHA);
        sb.setColor(new Color(1, 1, 1, 1));
        sb.draw(mask, POS_X, POS_Y, 50, 250, 100, 500);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sb.end();

        fbo.end();
        sb.begin();
        TextureRegion drawTex = new TextureRegion(fbo.getColorBufferTexture());
        drawTex.flip(false, true);
        sb.draw(drawTex, -Settings.VERT_LETTERBOX_AMT, -Settings.HORIZ_LETTERBOX_AMT);

    }

    public static void update() {
        for (VexMechanicBenefit b : costOneBenefits) {
            b.y += 1;
            if (b.y > POS_Y) {
                b.y = POS_Y - 300;
            }
        }
    }


    public static class VexMechanicBenefit {

        public String text;
        public Runnable effect;
        public int cost;

        public float x;
        public float y;

        public VexMechanicBenefit(String s, Runnable runnable, int cost) {
            this.text = s;
            this.effect = runnable;
            this.cost = cost;
            this.x = POS_X;
            this.y = POS_Y;
        }

        public void render(SpriteBatch sb) {
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.panelNameFont, text, x, y, Color.WHITE.cpy());
        }

        public enum VexMechanicBenefitCost {
            ONE,
            TWO,
            FIVE
        }
    }
}
