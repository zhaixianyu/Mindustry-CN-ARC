package mindustry.ai.types;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.units.AIController;
import mindustry.game.Waves;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.input.DesktopInput;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.type.unit.ErekirUnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;
import static mindustry.ui.Styles.black;
import static mindustry.ui.Styles.flatToggleMenut;

public class arcMinerAI extends AIController {
    private final Seq<Block> oreAllList = content.blocks().select(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null);
    private final Seq<Block> oreAllWallList = content.blocks().select(b -> ((b instanceof Floor f && f.wallOre) || b instanceof StaticWall) && b.itemDrop != null);
    private Seq<Block> oreList = content.blocks().select(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null);
    private Seq<Block> oreWallList = content.blocks().select(b -> ((b instanceof Floor f && f.wallOre) || b instanceof StaticWall) && b.itemDrop != null);
    public Seq<Item> canMineList;
    public boolean mining = true;
    public Item targetItem;
    public Tile ore;

    @Override
    public void init() {
        if (!unit.canMine()) return;

        if (unit.type.mineFloor) {
            canMineList = oreList.map(b -> b.itemDrop).select(i -> unit.canMine(i));
        } else if (unit.type.mineWalls) {
            canMineList = oreWallList.map(b -> b.itemDrop).select(i -> unit.canMine(i));
        }
    }

    private Item updateTargetItem(boolean canMineNonBuildable) {
        //reverse是因为min取最后一个最小的
        return canMineList.select(i -> (unit.type.mineFloor ? indexer.hasOre(i) : indexer.hasOreWall(i))
                && (canMineNonBuildable || i.buildable)
                && unit.core().acceptItem(null, i)
        ).reverse().min(i -> unit.core().items.get(i));
    }

    private Tile findClosetOre(Building build) {
        if (unit.type.mineFloor) {
            return indexer.findClosestOre(build.x, build.y, targetItem);
        }
        return indexer.findClosestWallOre(build.x, build.y, targetItem);
    }
    @Override
    public void updateMovement() {
        if (!unit.canMine() || canMineList.isEmpty() || unit.core() == null) return;

        CoreBlock.CoreBuild core = unit.closestCore();
        //变量命名不知道叫啥了
        //最近的可以塞入非建筑物品的核心
        CoreBlock.CoreBuild core2 = unit.team.data().cores.select(c -> !((CoreBlock) c.block).incinerateNonBuildable).min(c -> unit.dst(c));

        CoreBlock.CoreBuild targetCore = targetItem == null || targetItem.buildable || core2 == null ? core : core2;

        if (unit.type.canBoost) {
            player.boosting = true;
        }
        if (mining) {

            if (targetItem != null && (!core.acceptItem(null, targetItem) || (core2 == null && !targetItem.buildable))) {
                unit.mineTile = null;
                targetItem = null;
            }

            if (targetItem == null || timer.get(timerTarget2, 300f)) {
                targetItem = updateTargetItem(core2 != null);
                if (targetItem == null) return;
            }

            if (!unit.acceptsItem(targetItem) || unit.stack.amount >= unit.type.itemCapacity) {
                mining = false;
                return;
            }

            if (ore == null || !unit.validMine(ore, false) || ore.drop() != targetItem || timer.get(timerTarget3, 120f)) {
                ore = findClosetOre(targetCore);
                if (ore == null) return;
            }


            Tmp.v1.setLength(unit.type.mineRange * 0.9f).limit(ore.dst(targetCore) - 0.5f).setAngle(ore.angleTo(targetCore)).add(ore);
            moveTo(Tmp.v1, 0.1f);
            if (unit.validMine(ore)) {
                unit.mineTile = ore;
            }

        } else {
            unit.mineTile = null;

            if (unit.stack.amount == 0) {
                mining = true;
                return;
            }
            if (!core.acceptItem(null, unit.stack.item)) {
                unit.clearItem();
            }

            moveTo(targetCore, core.hitSize());
            if (unit.within(targetCore, itemTransferRange) && targetCore.acceptItem(null, targetItem)) {
                if (control.input instanceof DesktopInput di) {
                    di.autoAim = true;
                }
                unit.aimX = core.x;
                unit.aimY = core.y;
                Call.transferInventory(player, core);
                targetItem = updateTargetItem(core2 != null);
                if (control.input instanceof DesktopInput di) {
                    Time.run(30f, () -> di.autoAim = false);
                }
            }
        }
    }

    @Override
    public void updateVisuals() {
    }

    public void editorTable(){
        BaseDialog dialog = new BaseDialog("矿物选择器");
        Table table = dialog.cont;
        Runnable[] rebuild = {null};
        rebuild[0] = () -> {
            table.clear();

            table.table(c -> {
                c.add("地表矿").row();
                c.table(list -> {
                    int i = 0;
                    for (Block block : oreAllList) {
                        if(indexer.floorOresCount[block.id]==0) continue;
                        if (i++ % 3 == 0) list.row();
                        list.button(block.emoji() + "\n" + indexer.floorOresCount[block.id], flatToggleMenut, () -> {
                            if(oreList.contains(block)) oreList.remove(block);
                            else if(!oreList.contains(block)) oreList.add(block);
                            rebuild[0].run();
                        }).tooltip(block.localizedName).checked(oreList.contains(block)).width(100f).height(50f);
                    }
                }).row();
                c.add("墙矿").row();
                c.table(list -> {
                    int i = 0;
                    for (Block block : oreAllWallList) {
                        if(indexer.wallOresCount[block.id]==0) continue;
                        if (i++ % 3 == 0) list.row();
                        list.button(block.emoji() + "\n" + indexer.wallOresCount[block.id], flatToggleMenut, () -> {
                            if(oreWallList.contains(block)) oreWallList.remove(block);
                            else if(!oreWallList.contains(block)) oreWallList.add(block);
                            rebuild[0].run();
                        }).tooltip(block.localizedName).checked(oreWallList.contains(block)).width(100f).height(50f);
                    }
                }).row();
                c.row();
            });
        };
        rebuild[0].run();
        dialog.addCloseButton();
        dialog.show();
    }
}
