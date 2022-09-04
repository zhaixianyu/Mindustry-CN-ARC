package mindustry.arcModule;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.util.pooling.Pools;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.Radar;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.tilesize;

public class DrawUtilities {
    /**Trans From minerTool*/
    public static float arcDrawText(String text, float scl, float dx, float dy, Color color, int halign){
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(scl);
        layout.setText(font, text);

        float height = layout.height;

        font.setColor(color);
        font.draw(text, dx, dy + layout.height + 1, halign);

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);

        return height;
    }

    public static float arcDrawText(String text, float scl, float dx, float dy, int halign){
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(scl);
        layout.setText(font, text);

        float height = layout.height;

        font.draw(text, dx + layout.width/2, dy + layout.height/2, halign);

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);

        return height;
    }

    public static Effect arcBuildEffect(Building build){
        if(build == null || !Core.settings.getBool("arcPlacementEffect")) return new Effect();
        if(build.block instanceof BaseTurret baseTurret && build.health > Core.settings.getInt("blockbarminhealth"))
            return createBuildEffect(120f,baseTurret.range,build.team.color);
        else if(build.block instanceof MendProjector mendProjector)
            return createBuildEffect(120f,mendProjector.range, Pal.heal);
        else if(build.block instanceof OverdriveProjector overdriveProjector)
            return createBuildEffect(120f,overdriveProjector.range,overdriveProjector.baseColor);
        else if(build.block instanceof CoreBlock block)
            return createBuildEffect(180f,block.fogRadius * tilesize,build.team.color);
        else if(build.block instanceof Radar radar)
            return createBuildEffect(180f,radar.fogRadius * tilesize,build.team.color);
        else return new Effect();
    }

    public static Effect createBuildEffect(float lifeTime,float range,Color color){
        return new Effect(lifeTime, e -> {
            color(color);
            stroke((1.5f - e.fin() * 1f) * (range/100));
            if(e.finpow()<0.8f) Lines.circle(e.x, e.y, e.finpow()/0.8f * range);
            else {
                Draw.alpha((1-e.fin()) *5f);
                Lines.circle(e.x, e.y, range);}
        });
    }

}
