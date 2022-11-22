package mindustry.arcModule.toolpack;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Time;
import mindustry.arcModule.Marker;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Unit;
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
    private static float ratio = 10f;

    /**
     * 每多少范围一个雷达
     */
    private static float radarCir = 25f;
    private static final int basicRadarCir = 25;
    /**
     * 需要多少s扫描完成，仅用于特效
     */
    private static float scanTime = 5;
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
    private static float worldSize = 0;

    private static final Table t = new Table(Styles.black3);

    private static final float unitSize = 0.1f;
    private static final float playerSize = 3f * tilesize, markerSize = 15f * tilesize;
    private static int expandRate = 1;
    private static float time = 0;

    public static boolean mobileRadar = false;


    static {
        t.touchable = Touchable.disabled;
        t.margin(8f).add(">> 雷达扫描中 <<").color(getThemeColor()).style(Styles.outlineLabel).labelAlign(Align.center);
        t.visible = true;
        t.update(() -> t.setPosition(Core.graphics.getWidth() / 2f, Core.graphics.getHeight() * 0.1f, Align.center));
        t.pack();
        t.act(0.1f);
        Core.scene.add(t);

        Events.on(EventType.WorldLoadEvent.class, event -> {
            scanTime = Math.max(Mathf.dst(world.width(), world.height()) / 20f, 7.5f);
        });
    }

    public static void drawScanner() {
        if (Core.settings.getInt("radarMode") == 0) return;
        float extendSpd = Core.settings.getInt("radarMode") * 0.2f;
        Draw.reset();

        if(mobile){
            if (extendSpd >= 6) {
                t.visible = mobileRadar;
                scanRate = t.visible ? 1f : 0f;
            } else {
                if (mobileRadar) {
                    t.visible = true;
                    if (scanRate < 1f) scanRate = Math.min(scanRate + 1 / 60f / scanTime * extendSpd, 1f);
                } else {
                    t.visible = false;
                    if (scanRate > 0f) scanRate = Math.max(scanRate - 3 / 60f / scanTime * extendSpd, 0f);
                }
            }
        }else{
            if (extendSpd >= 6) {
                if (Core.input.keyDown(Binding.arcDetail) && Time.time - time > 60f) {
                    time = Time.time;
                    t.visible = !t.visible;
                    scanRate = t.visible ? 1f : 0f;
                }
            } else {
                if (Core.input.keyDown(Binding.arcDetail)) {
                    t.visible = true;
                    if (scanRate < 1f) scanRate = Math.min(scanRate + 1 / 60f / scanTime * extendSpd, 1f);
                } else {
                    t.visible = false;
                    if (scanRate > 0f) scanRate = Math.max(scanRate - 3 / 60f / scanTime * extendSpd, 0f);
                }
            }
        }

        if (scanRate <= 0) return;

        float playerToBorder = Math.max(Math.max(Math.max(Mathf.dst(player.tileX(), player.tileY()), Mathf.dst(world.width() - player.tileX(), player.tileY())), Mathf.dst(world.width() - player.tileX(), world.height() - player.tileY())), Mathf.dst(player.tileX(), world.height() - player.tileY()));
        worldSize = Math.min(playerToBorder, (int) (Mathf.dst(world.width(), world.height()) / radarCir) * radarCir);
        expandRate = (int) (worldSize / basicRadarCir / 10 + 1);
        radarCir = expandRate * basicRadarCir;  //地图越大，radar间隔越大。此处选择最多10圈
        curScanRange = worldSize * tilesize * scanRate;

        for (int i = 1; i < curScanRange / radarCir / tilesize + 1; i++) {
            Draw.color(player.team().color, 0.45f);
            Lines.stroke(expandRate * 0.75f);
            Lines.circle(player.x, player.y, (radarCir * i * tilesize) / ratio);
            float rRatio = (radarCir * i * tilesize) / ratio + 2f;
            arcDrawText(i * (int) radarCir + "", 0.2f / Scl.scl(1f) * expandRate, player.x, player.y + rRatio, getThemeColor(), 1);
            arcDrawText(i * (int) radarCir + "", 0.2f / Scl.scl(1f) * expandRate, player.x + rRatio * Mathf.cos(Mathf.PI * 7 / 6), player.y + rRatio * Mathf.sin(Mathf.PI * 7 / 6), getThemeColor(), 1);
            arcDrawText(i * (int) radarCir + "", 0.2f / Scl.scl(1f) * expandRate, player.x + rRatio * Mathf.cos(Mathf.PI * 11 / 6), player.y + rRatio * Mathf.sin(Mathf.PI * 11 / 6), getThemeColor(), 1);
        }

        if (scanRate < 1f) {
            Draw.color(player.team().color, 0.8f);
            Lines.stroke(expandRate);
            Lines.circle(player.x, player.y, curScanRange / ratio);
            Draw.color(player.team().color, 0.1f);
            Fill.circle(player.x, player.y, curScanRange / ratio);
        } else {
            curScanRange = (int) (curScanRange / radarCir / tilesize + 1) * radarCir * tilesize;

            Draw.color(player.team().color, 0.1f);
            Fill.circle(player.x, player.y, curScanRange / ratio);

            Draw.color(player.team().color, 0.6f);
            float curve = Mathf.curve(Time.time % 360f, 120f, 360f);
            Lines.stroke(expandRate * 1.5f);
            Lines.circle(player.x, player.y, curScanRange * Interp.pow3Out.apply(curve) / ratio);
            Lines.stroke(expandRate * 1.5f);

            Draw.color(player.team().color, 0.1f);
            Fill.rect(player.x - player.x / ratio + world.width() * tilesize / ratio / 2, player.y - player.y / ratio + world.height() * tilesize / ratio / 2, world.width() * tilesize / ratio, world.height() * tilesize / ratio);
            Draw.color(player.team().color, 0.85f);
            Lines.rect(player.x - player.x / ratio, player.y - player.y / ratio, world.width() * tilesize / ratio, world.height() * tilesize / ratio);
        }

        Draw.color(player.team().color, 0.8f);
        Lines.line(player.x, player.y, player.x + curScanRange * Mathf.cos(Time.time * scanSpeed) / ratio, player.y + curScanRange * Mathf.sin(Time.time * scanSpeed) / ratio);
        Draw.reset();

        // 出怪点
        if (spawner.countSpawns() < 25 && !state.rules.pvp) {
            for (Tile tile : spawner.getSpawns()) {
                if (scanRate < 1f && Mathf.dst(tile.worldx() - player.x, tile.worldy() - player.y) > curScanRange)
                    continue;

                Draw.color(state.rules.waveTeam.color, 1f);
                arcDrawNearby(Icon.units.getRegion(), tile, Math.max(6 * expandRate, state.rules.dropZoneRadius / ratio / 2), state.rules.waveTeam.color);

                float curve = Mathf.curve(Time.time % 200f, 60f, 200f);
                Draw.color(state.rules.waveTeam.color, 1f);
                Lines.stroke(expandRate * 1f);
                Lines.circle(player.x + (tile.worldx() - player.x) / ratio, player.y + (tile.worldy() - player.y) / ratio, state.rules.dropZoneRadius * Interp.pow3Out.apply(curve) / ratio);
                Draw.color(state.rules.waveTeam.color, 0.5f);
                Lines.stroke(expandRate * 0.8f);
                Lines.dashCircle(player.x + (tile.worldx() - player.x) / ratio, player.y + (tile.worldy() - player.y) / ratio, state.rules.dropZoneRadius * Interp.pow3Out.apply(curve) / ratio);
            }
        }
        //绘制核心
        for (Team team : Team.all) {
            for (CoreBlock.CoreBuild core : team.cores()) {
                if (scanRate < 1f && Mathf.dst(core.x - player.x, core.y - player.y) > curScanRange) continue;
                if (state.rules.pvp && core.inFogTo(player.team())) continue;
                Draw.color(core.team.color, 1f);
                arcDrawNearby(core.block.fullIcon, core.tile, 4 * expandRate, core.team.color);
            }
        }
        //绘制单位
        for (Unit unit : Groups.unit) {
            if (scanRate < 1f && Mathf.dst(unit.x - player.x, unit.y - player.y) > curScanRange) continue;
            Draw.color(unit.team.color, 0.6f);
            Fill.circle(transX(unit.x), transY(unit.y), unit.hitSize * unitSize);
        }
        //绘制玩家
        for (Player unit : Groups.player) {
            if (scanRate < 1f && Mathf.dst(unit.x - player.x, unit.y - player.y) > curScanRange) continue;

            Draw.color(unit.team().color, 0.9f);

            float angle = unit.unit().rotation * Mathf.degreesToRadians;
            Fill.tri(transX(unit.x + Mathf.cos(angle) * playerSize * expandRate), transY(unit.y + Mathf.sin(angle) * playerSize * expandRate),
                    transX(unit.x + Mathf.cos(angle + Mathf.PI * 2 / 3) * playerSize * expandRate * 0.75f), transY(unit.y + Mathf.sin(angle + Mathf.PI * 2 / 3) * playerSize * expandRate * 0.75f),
                    transX(unit.x + Mathf.cos(angle + Mathf.PI * 4 / 3) * playerSize * expandRate * 0.75f), transY(unit.y + Mathf.sin(angle + Mathf.PI * 4 / 3) * playerSize * expandRate * 0.75f));
        }
        //绘制arc标记
        if (Marker.markList.size > 0) {
            Marker.markList.each(a -> {
                if ((Time.time - a.time) > Marker.retainTime) return;
                Draw.color(a.markType.color);
                Lines.stroke(expandRate * (1 - (Time.time % 180 + 30) / 210));

                Lines.circle(transX(a.markPos.x), transY(a.markPos.y), markerSize / ratio * (Time.time % 180) / 180);
                Lines.stroke(expandRate);
                Lines.circle(transX(a.markPos.x), transY(a.markPos.y), markerSize / ratio);
                Lines.arc(transX(a.markPos.x), transY(a.markPos.y), (markerSize - expandRate) / ratio, 1 - (Time.time - a.time) / Marker.retainTime);
                Draw.reset();
            });
        }

    }

    /**
     * 以玩家为中心，绘制一个环绕的图标圈
     */

    public static void arcDrawNearbyRot(TextureRegion region, Tile tile, float size, boolean rotate, Color color) {
        float range = Mathf.dst(tile.worldy() - player.y, tile.worldx() - player.x);
        if (range > curScanRange) return;

        float rot = (float) Math.atan2(tile.worldy() - player.y, tile.worldx() - player.x);
        float nx = player.x + Mathf.cos(rot) * range / ratio;
        float ny = player.y + Mathf.sin(rot) * range / ratio;
        if (rotate) Draw.rect(region, nx, ny, size, size, (float) Math.toDegrees(rot));
        else Draw.rect(region, nx, ny, size, size);
        arcDrawText((int) (range / 8f) + "", 0.2f / Scl.scl(1f) * expandRate, nx, ny + size / 2, color, 1);
    }

    public static void arcDrawNearby(TextureRegion region, Tile tile, float size, Color color) {
        float range = Mathf.dst(tile.worldy() - player.y, tile.worldx() - player.x);
        if (range > curScanRange) return;
        float nx = player.x + (tile.worldx() - player.x) / ratio;
        float ny = player.y + (tile.worldy() - player.y) / ratio;
        Draw.rect(region, nx, ny, size, size);
        //Fill.rect(nx, ny, size, size);
        //arcDrawText((int) (range / 8f) + "", 0.2f / Scl.scl(1f) * expandRate, nx, ny + size / 2, color, 1);
    }

    private static float transX(float x) {
        return player.x + (x - player.x) / ratio;
    }

    private static float transY(float y) {
        return player.y + (y - player.y) / ratio;
    }

}
