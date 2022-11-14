package mindustry.arcModule.toolpack;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.input.Binding;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;
import static mindustry.arcModule.DrawUtilities.*;

public class arcScanner {

    /**
     * 实际与画面的距离
     */
    private static int ratio = 20;

    /**
     * 每多少范围一个雷达
     */
    private static int radarCir = 25;
    private static final int basicRadarCir = 25;
    /**
     * 需要多少s扫描完成，仅用于特效
     */
    private static float scanTime = 3;
    /**
     * 当前扫描的百分比
     */
    private static float scanRate = 0;

    /**
     * 当前扫描的百分比
     */
    private static float scanSpeed = -0.02f;

    /**
     * 实际扫描范围
     */
    private static float curScanRange = 0;
    private static int worldSize = 0;

    private static final Table t = new Table(Styles.black3);


    static {
            t.touchable = Touchable.disabled;
            t.margin(8f).add(">> 扫描模式已启动 <<").color(getThemeColor()).style(Styles.outlineLabel).labelAlign(Align.center);
            t.visible = true;
            t.update(() -> t.setPosition(Core.graphics.getWidth()/2f, Core.graphics.getHeight() * 0.8f, Align.center));
            t.pack();
            t.act(0.1f);
            Core.scene.add(t);
    }

    public static void drawScanner() {
        if (Core.input.keyDown(Binding.arcDetail)) {
            Draw.reset();
            t.visible = true;
            radarCir = (worldSize / basicRadarCir / 10 + 1) * basicRadarCir;

            if (scanRate < 1f) scanRate += 1 / 60f / scanTime;

            worldSize = (int)(Mathf.dst(world.width(), world.height()) / radarCir) * radarCir;
            curScanRange = worldSize * tilesize * scanRate;

            for (int i = 1; i < curScanRange / radarCir / tilesize; i++) {
                Draw.color(player.team().color, 0.6f);
                Lines.circle(player.x, player.y, (float) (radarCir * i * tilesize) / ratio);
                arcDrawText(i * radarCir + "", 0.2f / Scl.scl(1f), player.x, player.y + i * radarCir * tilesize / ratio + 1f, getThemeColor(), 1);
            }

            if (scanRate < 1f) {
                Draw.color(player.team().color, 0.8f);
                Lines.circle(player.x, player.y, curScanRange / ratio);
            } else {
                Draw.color(player.team().color, 0.6f);
                float curve = Mathf.curve(Time.time % 240f, 120f, 240f);
                Lines.circle(player.x, player.y, curScanRange * Interp.pow3Out.apply(curve));
            }
            Lines.line(player.x, player.y, player.x + curScanRange * Mathf.cos(Time.time * scanSpeed) / ratio, player.y + curScanRange * Mathf.sin(Time.time * scanSpeed) / ratio);
            Draw.reset();

            // 出怪点
            if (spawner.countSpawns() < 25 && !state.rules.pvp) {
                for (Tile tile : spawner.getSpawns()) {
                    Draw.color(state.rules.waveTeam.color,1f);
                    arcDrawNearby(Icon.units.getRegion(), tile, 6, false, state.rules.waveTeam.color);
                }
            }

            for (Team team : Team.all) {
                for (CoreBlock.CoreBuild core : team.cores()) {
                    if (state.rules.pvp && core.inFogTo(player.team())) continue;
                    Draw.color(core.team.color, 0.8f);
                    arcDrawNearby(core.block.fullIcon, core.tile, 4, true, core.team.color);
                }
            }
        } else {
            t.visible = false;
            scanRate = 0;
        }
    }

    /**
     * 以玩家为中心，绘制一个环绕的图标圈
     */

    public static void arcDrawNearby(TextureRegion region, Tile tile, float size, boolean rotate, Color color) {
        float range = Mathf.dst(tile.worldy() - player.y, tile.worldx() - player.x);
        if (range > curScanRange) return;

        float rot = (float) Math.atan2(tile.worldy() - player.y, tile.worldx() - player.x);
        float nx = player.x + Mathf.cos(rot) * range / ratio;
        float ny = player.y + Mathf.sin(rot) * range / ratio;
        if (rotate) Draw.rect(region, nx, ny, size, size, (float) Math.toDegrees(rot));
        else Draw.rect(region, nx, ny, size, size);
        arcDrawText((int) (range / 8f) + "", 0.2f / Scl.scl(1f), nx, ny + size / 2, color, 1);
    }

}
