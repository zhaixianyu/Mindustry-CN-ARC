package mindustry.arcModule.toolpack;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.BlockGroup;


import static mindustry.Vars.*;
import static mindustry.arcModule.RFuncs.calWaveTimer;

public class arcScanMode {

    private static Table st = new Table(Styles.black3);

    private static Table ct = new Table(Styles.black3);
    private static Table ctTable = new Table();
    /**
     * spawner
     */
    private static final Table spt = new Table();
    private static Table spawnerTable = new Table();
    static float thisAmount, thisHealth, thisEffHealth, thisDps;
    static int totalAmount = 0, totalHealth = 0, totalEffHealth = 0, totalDps = 0;

    static int tableCount = 0;
    /**
     * conveyor
     */
    static final int maxLoop = 200;


    static {
        {
            st.touchable = Touchable.disabled;
            st.margin(8f).add(">> 扫描详情模式 <<").color(getThemeColor()).style(Styles.outlineLabel).labelAlign(Align.center);
            st.update(() -> st.setPosition(Core.graphics.getWidth() / 2f, Core.graphics.getHeight() * 0.7f, Align.center));
            st.pack();
            st.act(0.1f);
            st.update(() -> st.visible = control.input.arcScanMode && state.isPlaying());
            Core.scene.add(st);
        }
        {
            ct.touchable = Touchable.disabled;
            ct.visible = false;
            ct.add(ctTable).margin(8f);
            ct.pack();
            Core.scene.add(ct);
        }
        {
            spt.touchable = Touchable.disabled;
            spt.visible = false;
            spt.add(spawnerTable).margin(8f);
            spt.pack();
            Core.scene.add(spt);
        }
    }

    public static void arcScan() {
        detailCursor();
        detailSpawner();
        detailTransporter();
    }

    private static void detailCursor() {
        ct.visible = ct.visible && state.isPlaying();
        ctTable.clear();
        if (!control.input.arcScanMode) {
            ct.visible = false;
            return;
        }
        ct.setPosition(Core.input.mouseX(), Core.input.mouseY());
        ct.visible = true;
        ctTable.table(ctt -> {
            ctt.add((int) (Core.input.mouseWorldX() / 8) + "," + (int) (Core.input.mouseWorldY() / 8));
            ctt.row();
            ctt.add("距离：" + (int) (Mathf.dst(player.x, player.y, Core.input.mouseWorldX(), Core.input.mouseWorldY()) / 8));
        });
    }

    private static void detailSpawner() {
        spt.visible = spt.visible && state.isPlaying();
        if (!control.input.arcScanMode) {
            spt.visible = false;
            spawnerTable.clear();
            return;
        }
        totalAmount = 0;
        totalHealth = 0;
        totalEffHealth = 0;
        totalDps = 0;
        int curInfoWave = state.wave + 1;
        for (Tile tile : spawner.getSpawns()) {
            if (Mathf.dst(tile.worldx(), tile.worldy(), Core.input.mouseWorldX(), Core.input.mouseWorldY()) < state.rules.dropZoneRadius) {
                Draw.z(Layer.effect - 2f);
                Draw.color(state.rules.waveTeam.color);
                Lines.stroke(4f);
                float curve = Mathf.curve(Time.time % 360f, 120f, 300f);
                if (curve > 0) Lines.circle(tile.worldx(), tile.worldy(), state.rules.dropZoneRadius * curve);
                Lines.circle(tile.worldx(), tile.worldy(), state.rules.dropZoneRadius);
                Lines.arc(tile.worldx(), tile.worldy(), state.rules.dropZoneRadius - 3f, state.wavetime / state.rules.waveSpacing, 90f);
                float angle = Mathf.pi / 2 + state.wavetime / state.rules.waveSpacing * 2 * Mathf.pi;
                Draw.color(state.rules.waveTeam.color);
                Fill.circle(tile.worldx() + state.rules.dropZoneRadius * Mathf.cos(angle), tile.worldy() + state.rules.dropZoneRadius * Mathf.sin(angle), 8f);


                Vec2 v = Core.camera.project(tile.worldx(), tile.worldy());
                spt.setPosition(v.x, v.y);
                spt.visible = true;
                spawnerTable.clear();
                spawnerTable.table(Styles.black3, tt -> {
                    tt.add(calWaveTimer()).row();
                    state.rules.spawns.each(group -> group.spawn == -1 || (tile.x == Point2.x(group.spawn) && tile.y == Point2.y(group.spawn)), group -> {
                        thisAmount = group.getSpawned(curInfoWave);
                        if (thisAmount > 0) {
                            thisHealth = (group.type.health + group.getShield(curInfoWave)) * thisAmount;
                            if (group.effect == null) {
                                thisEffHealth = (group.type.health + group.getShield(curInfoWave)) * thisAmount;
                                thisDps = group.type.estimateDps();
                            } else {
                                thisEffHealth = group.effect.healthMultiplier * (group.type.health + group.getShield(curInfoWave)) * thisAmount;
                                thisDps = group.effect.damageMultiplier * group.effect.reloadMultiplier * group.type.estimateDps();
                            }
                            totalAmount += thisAmount;
                            totalHealth += thisHealth;
                            totalEffHealth += thisEffHealth;
                            totalDps += thisDps;
                        }
                    });
                    if (totalAmount == 0) tt.add("该波次没有敌人");
                    else {
                        tt.table(wi -> {
                            wi.add("\uE86D").width(50f);
                            wi.add("[accent]" + totalAmount).growX().row();
                            wi.add("\uE813").width(50f);
                            wi.add("[accent]" + UI.formatAmount(totalHealth, 2)).growX().row();
                            if (totalEffHealth != totalHealth) {
                                wi.add("\uE810").width(50f);
                                wi.add("[accent]" + UI.formatAmount(totalEffHealth, 2)).growX().row();
                            }
                            wi.add("\uE86E").width(50f);
                            wi.add("[accent]" + UI.formatAmount(totalDps, 2)).growX();
                        });
                    }
                    tt.row();
                    tableCount = 0;
                    tt.table(wi -> state.rules.spawns.each(group -> group.spawn == -1 || (tile.x == Point2.x(group.spawn) && tile.y == Point2.y(group.spawn)), group -> {
                        tableCount += 1;
                        if (tableCount % 10 == 0) wi.row();
                        int amount = group.getSpawned(curInfoWave);
                        if (amount > 0) {
                            StringBuilder groupInfo = new StringBuilder();
                            groupInfo.append(group.type.emoji());

                            groupInfo.append(group.type.typeColor());

                            groupInfo.append("\n").append(amount);
                            groupInfo.append("\n");

                            if (group.getShield(curInfoWave) > 0f)
                                groupInfo.append(UI.formatAmount((long) group.getShield(curInfoWave)));
                            groupInfo.append("\n[]");
                            if (group.effect != null && group.effect != StatusEffects.none)
                                groupInfo.append(group.effect.emoji());
                            if (group.items != null && group.items.amount > 0)
                                groupInfo.append(group.items.item.emoji());
                            if (group.payloads != null && group.payloads.size > 0)
                                groupInfo.append("\uE87B");
                            wi.add(groupInfo.toString()).height(130f).width(50f);
                        }
                    })).scrollX(true).scrollY(false).maxWidth(mobile ? 400f : 750f).growX();
                });
                return;
            }
        }

        spt.visible = false;
        spawnerTable.clear();
    }

    private static void detailTransporter() {
        if (!control.input.arcScanMode) return;

        //check tile being hovered over
        Tile hoverTile = world.tileWorld(Core.input.mouseWorld().x, Core.input.mouseWorld().y);
        if (hoverTile == null || hoverTile.build == null || !hoverTile.build.displayable() || hoverTile.build.inFogTo(player.team())) {
            return;
        }

        int forwardLoop = 0, backwardLoop = 0;

        Building nextA = hoverTile.build;
        Building next = hoverTile.build.front();

        while (next != null && next.team == hoverTile.build.team && forwardLoop < maxLoop) {
            forwardLoop += 1;
            Drawf.selected(next, Tmp.c1.set(Color.red).a(Mathf.absin(4f, 1f) * 0.5f + 0.5f));

            if (next.block.group != BlockGroup.transportation) break;  // 不是运输方块不传递

            if (next instanceof OverflowGate.OverflowGateBuild || next instanceof Sorter.SorterBuild ||
                    next instanceof Router.RouterBuild || next instanceof DuctRouter.DuctRouterBuild) {
                int from = next.relativeToEdge(nextA.tile);
                Seq<Building> toBuild = new Seq<>();
                if (!(next instanceof Sorter.SorterBuild sb && !((Sorter) sb.block).invert && sb.sortItem == null)) toBuild.add(next.nearby((from + 1) % 4));
                if (!(next instanceof Sorter.SorterBuild sb && ((Sorter) sb.block).invert && sb.sortItem == null)) toBuild.add(next.nearby((from + 2) % 4));
                if (!(next instanceof Sorter.SorterBuild sb && !((Sorter) sb.block).invert && sb.sortItem == null)) toBuild.add(next.nearby((from + 3) % 4));

                Building nextBuild = toBuild.select(building -> building != null && canAccept(building.block)).random();
                nextA = next;
                next = nextBuild;
            } else {
                Building nextBuild = next.front();
                if (next instanceof DuctBridge.DuctBridgeBuild ductBridgeBuild && ductBridgeBuild.findLink() != null){
                    nextBuild = ductBridgeBuild.findLink();
                } else if (next instanceof ItemBridge.ItemBridgeBuild itemBridgeBuild && world.tile(itemBridgeBuild.link) != null)
                    nextBuild = world.tile(itemBridgeBuild.link).build;

                if (nextBuild == null) break;
                if (!canAccept(nextBuild.block)) break;
                nextA = next;
                next = nextBuild;
            }

        }
    }

    private static boolean canAccept(Block block) {
        if (block.group == BlockGroup.transportation) return true;
        for (Item item : content.items()) {
            if (block.consumesItem(item) || block.itemCapacity > 0) {
                return true;
            }
        }
        return false;
    }

}