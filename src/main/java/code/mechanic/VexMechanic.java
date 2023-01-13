package code.mechanic;

import code.util.ImageHelper;
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
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;

import java.util.ArrayList;

import static code.util.Wiz.*;

public class VexMechanic {


    private static final float POS_X = Settings.WIDTH / 2 - (400F * Settings.scale);
    private static final float POS_Y = Settings.HEIGHT / 3 + (200F * Settings.scale);

    private static FrameBuffer fbo;

    public static void init() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
    }

    public static ArrayList<VexMechanicBenefit> costOneBenefits = new ArrayList<>();
    public static ArrayList<VexMechanicBenefit> costTwoBenefits = new ArrayList<>();
    public static ArrayList<VexMechanicBenefit> costFiveBenefits = new ArrayList<>();

    private static final float OPTIONS_ONE_STARTY = POS_Y + (270 * Settings.scale);
    private static final float OPTIONS_TWO_STARTY = POS_Y + (170 * Settings.scale);
    private static final float OPTIONS_FIVE_STARTY = POS_Y + (70 * Settings.scale);


    static {
        costOneBenefits.add(new VexMechanicBenefit("Draw 2 cards.", () -> atb(new DrawCardAction(2)), 1));
        costOneBenefits.add(new VexMechanicBenefit("Deal 5 damage to ALL enemies.", () -> atb(new DamageAllEnemiesAction(AbstractDungeon.player, DamageInfo.createDamageMatrix(5, true), DamageInfo.DamageType.THORNS, AbstractGameAction.AttackEffect.FIRE)), 1));
        costOneBenefits.add(new VexMechanicBenefit("Exhaust 1 card. Draw 1 card.", () -> {
            atb(new ExhaustAction(1, false));
            atb(new DrawCardAction(1));
        }, 1));
        costOneBenefits.add(new VexMechanicBenefit("Gain 1 Strength.", () -> applyToSelf(new StrengthPower(AbstractDungeon.player, 1)), 1));
        costOneBenefits.add(new VexMechanicBenefit("Gain 1 Energy.", () -> atb(new GainEnergyAction(1)), 1));
        costOneBenefits.add(new VexMechanicBenefit("Apply 1 Weak and Vulnerable.", () -> {
            AbstractMonster tar = AbstractDungeon.getRandomMonster();
            applyToEnemy(tar, new WeakPower(tar, 1, false));
            applyToEnemy(tar, new VulnerablePower(tar, 1, false));
        }, 1));
        for (VexMechanicBenefit b : costOneBenefits) {
            b.x = POS_X + (50 * Settings.scale);
            b.y = POS_Y + (270 * Settings.scale) + costOneBenefits.indexOf(b) * (40 * Settings.scale);
        }


        costTwoBenefits.add(new VexMechanicBenefit("Gain 1 Strength and Dexterity.", null, 2));
        costTwoBenefits.add(new VexMechanicBenefit("Play the top card of your draw pile.", null, 2));
        costTwoBenefits.add(new VexMechanicBenefit("Gain 9 Block.", null, 2));
        costTwoBenefits.add(new VexMechanicBenefit("Gain 3 Energy.", null, 2));
        costTwoBenefits.add(new VexMechanicBenefit("Your next card is played twice.", null, 2));
        costTwoBenefits.add(new VexMechanicBenefit("Pick a card to draw.", null, 2));

        for (VexMechanicBenefit b : costTwoBenefits) {
            b.x = POS_X + (50 * Settings.scale);
            b.y = POS_Y + (170 * Settings.scale) + costTwoBenefits.indexOf(b) * (40 * Settings.scale);
        }

        costFiveBenefits.add(new VexMechanicBenefit("Play a card from your draw pile twice.", null, 5));
        costFiveBenefits.add(new VexMechanicBenefit("Gain 1 Intangible.", null, 5));
        costFiveBenefits.add(new VexMechanicBenefit("Gain 4 Strength and Dexterity.", null, 5));
        costFiveBenefits.add(new VexMechanicBenefit("Gain 3 Energy. Draw 3 cards.", null, 5));
        costFiveBenefits.add(new VexMechanicBenefit("Deal 33 damage to ALL enemies.", null, 5));

        for (VexMechanicBenefit b : costFiveBenefits) {
            b.x = POS_X + (50 * Settings.scale);
            b.y = POS_Y + (70 * Settings.scale) + costFiveBenefits.indexOf(b) * (40 * Settings.scale);
        }

    }

    public static VexMechanicBenefit cost1;
    public static VexMechanicBenefit cost2;
    public static VexMechanicBenefit cost5;

    private static float spinTimer1 = 0F;
    private static float spinTimer2 = 0F;
    private static float spinTimer5 = 0F;

    public static void atStartOfCombat() {
        cost1 = Wiz.getRandomItem(costOneBenefits, AbstractDungeon.cardRandomRng);
        spinTimer1 = MathUtils.random(9F, 11F);
        cost2 = Wiz.getRandomItem(costTwoBenefits, AbstractDungeon.cardRandomRng);
        spinTimer2 = MathUtils.random(11F, 13F);
        cost5 = Wiz.getRandomItem(costFiveBenefits, AbstractDungeon.cardRandomRng);
        spinTimer5 = MathUtils.random(13F, 15F);
    }


    private static Texture panelBG = TexLoader.getTexture("vexmechanicResources/images/ui/panelBg.png");
    private static Texture mask1 = TexLoader.getTexture("vexmechanicResources/images/ui/rowMask1.png");
    private static Texture mask2 = TexLoader.getTexture("vexmechanicResources/images/ui/rowMask2.png");
    private static Texture mask5 = TexLoader.getTexture("vexmechanicResources/images/ui/rowMask3.png");
    private static Texture cube_icon = TexLoader.getTexture("vexmechanicResources/images/ui/Cube_icon.png");

    public static void render(SpriteBatch sb) {
        ImageHelper.drawTextureScaled(sb, panelBG, POS_X, POS_Y, 1); // Render BG

        // Render Title Text
        FontHelper.renderFontCentered(sb, FontHelper.dungeonTitleFont, "Ticket Shop", POS_X + (333 * Settings.scale), POS_Y + (300 * Settings.scale), Color.WHITE.cpy(), 0.4F);

        // Render shuffler slot for bonus 1

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
        ImageHelper.drawTextureScaled(sb, mask1, 0, 0, 1);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sb.end();

        fbo.end();
        sb.begin();
        TextureRegion drawTex = new TextureRegion(fbo.getColorBufferTexture());
        drawTex.flip(false, true);
        sb.draw(drawTex, -Settings.VERT_LETTERBOX_AMT, -Settings.HORIZ_LETTERBOX_AMT);


        // Render shuffler slot for bonus 2

        sb.setColor(Color.WHITE.cpy());
        sb.end();
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glColorMask(true, true, true, true);
        sb.begin();

        sb.setColor(Color.WHITE.cpy());
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (VexMechanicBenefit b : costTwoBenefits) {
            b.render(sb);
        }

        sb.setBlendFunction(0, GL20.GL_SRC_ALPHA);
        sb.setColor(new Color(1, 1, 1, 1));
        ImageHelper.drawTextureScaled(sb, mask2, 0, 0, 1);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sb.end();

        fbo.end();
        sb.begin();
        drawTex = new TextureRegion(fbo.getColorBufferTexture());
        drawTex.flip(false, true);
        sb.draw(drawTex, -Settings.VERT_LETTERBOX_AMT, -Settings.HORIZ_LETTERBOX_AMT);

        // Render shuffler slot for bonus 5

        sb.setColor(Color.WHITE.cpy());
        sb.end();
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glColorMask(true, true, true, true);
        sb.begin();

        sb.setColor(Color.WHITE.cpy());
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (VexMechanicBenefit b : costFiveBenefits) {
            b.render(sb);
        }

        sb.setBlendFunction(0, GL20.GL_SRC_ALPHA);
        sb.setColor(new Color(1, 1, 1, 1));
        ImageHelper.drawTextureScaled(sb, mask5, 0, 0, 1);
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sb.end();

        fbo.end();
        sb.begin();
        drawTex = new TextureRegion(fbo.getColorBufferTexture());
        drawTex.flip(false, true);
        sb.draw(drawTex, -Settings.VERT_LETTERBOX_AMT, -Settings.HORIZ_LETTERBOX_AMT);

        // Render Costs

        ImageHelper.drawTextureScaled(sb, cube_icon, POS_X + (575 * Settings.scale), POS_Y + (245 * Settings.scale), 0.66F);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.panelNameFont, "1", POS_X + (575 * Settings.scale), POS_Y + (266 * Settings.scale), Color.WHITE.cpy());

        ImageHelper.drawTextureScaled(sb, cube_icon, POS_X + (575 * Settings.scale), POS_Y + (150 * Settings.scale), 0.66F);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.panelNameFont, "2", POS_X + (575 * Settings.scale), POS_Y + (155 * Settings.scale), Color.WHITE.cpy());

        ImageHelper.drawTextureScaled(sb, cube_icon, POS_X + (575 * Settings.scale), POS_Y + (75 * Settings.scale), 0.66F);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.panelNameFont, "5", POS_X + (575 * Settings.scale), POS_Y + (75 * Settings.scale), Color.WHITE.cpy());

    }

    private static final float TIME_MULT = 3;

    public static void update() {
        if (spinTimer1 > 0) {
            for (VexMechanicBenefit b : costOneBenefits) {
                b.y -= spinTimer1;
                if (b.y < OPTIONS_ONE_STARTY - 150F) {
                    b.y += 300F;
                }
            }
            spinTimer1 -= Gdx.graphics.getDeltaTime() * TIME_MULT;
            if (spinTimer1 <= 5F && cost1.y <= POS_Y + 275 && cost1.y >= POS_Y + 265) {
                spinTimer1 = 0;
            }
        }

        if (spinTimer2 > 0) {
            for (VexMechanicBenefit b : costTwoBenefits) {
                b.y -= spinTimer2;
                if (b.y < OPTIONS_TWO_STARTY - 150F) {
                    b.y += 300F;
                }
            }
            spinTimer2 -= Gdx.graphics.getDeltaTime() * TIME_MULT;
            if (spinTimer2 <= 5F && cost2.y <= POS_Y + 175 && cost2.y >= POS_Y + 170) {
                spinTimer2 = 0;
            }
        }

        if (spinTimer5 > 0) {
            for (VexMechanicBenefit b : costFiveBenefits) {
                b.y -= spinTimer5;
                if (b.y < OPTIONS_FIVE_STARTY - 150F) {
                    b.y += 300F;
                }
            }
            spinTimer5 -= Gdx.graphics.getDeltaTime() * TIME_MULT;
            if (spinTimer5 <= 5F && cost5.y <= POS_Y + (105 * Settings.scale) && cost5.y >= POS_Y + (95 * Settings.scale)) {
                spinTimer5 = 0;
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
            Color c = Color.WHITE.cpy();
            if (((cost1 == this && this.cost == 1 && spinTimer1 <= 0) || (cost2 == this && this.cost == 2 && spinTimer2 <= 0) || (cost5 == this && this.cost == 5 && spinTimer5 <= 0))) {
                c = Color.YELLOW.cpy();
            }
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.panelNameFont, text, x, y, c);
        }
    }
}
