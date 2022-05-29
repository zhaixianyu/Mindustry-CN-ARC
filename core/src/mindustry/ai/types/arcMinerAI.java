package mindustry.ai.types;

import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class arcMinerAI extends AIController {
    private final Seq<Block> oreList = content.blocks().select(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null);
    private final Seq<Block> oreWallList = content.blocks().select(b -> ((b instanceof Floor f && f.wallOre) || b instanceof StaticWall) && b.itemDrop != null);
    private Seq<Item> canMineList;
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

    @Override
    public void updateMovement() {
        if (!unit.canMine() || canMineList.isEmpty() || unit.core() == null) return;

        CoreBlock.CoreBuild core = unit.closestCore();
        //变量命名不知道叫啥了
        //最近的可以塞入非建筑物品的核心
        CoreBlock.CoreBuild core2 = unit.team.data().cores.select(c -> !((CoreBlock) c.block).incinerateNonBuildable).min(c -> unit.dst(c));

        CoreBlock.CoreBuild targetCore = targetItem == null || targetItem.buildable || core2 == null ? core : core2;

        if (mining) {

            if (targetItem != null && (!core.acceptItem(null, targetItem) || (core2 == null && !targetItem.buildable))) {
                unit.mineTile = null;
                targetItem = null;
            }

            if (targetItem == null || timer.get(timerTarget2, 120f)) {
                targetItem = updateTargetItem(core2 != null);
                if (targetItem == null) return;
            }

            if (!unit.acceptsItem(targetItem) || unit.stack.amount >= unit.type.itemCapacity) {
                mining = false;
                return;
            }

            if (ore == null || !unit.validMine(ore, false) || ore.drop() != targetItem || timer.get(timerTarget3, 120f)) {
                ore = unit.type.mineFloor ? indexer.findClosestOre(targetCore.x, targetCore.y, targetItem) : indexer.findClosestWallOre(targetCore.x, targetCore.y, targetItem);
                if (ore == null) return;
            }


            Tmp.v1.setLength(unit.type.mineRange * 0.9f).limit(ore.dst(targetCore) - 0.5f).setAngle(ore.angleTo(targetCore)).add(ore);
            moveTo(Tmp.v1, 0.1f, 5f);
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
                Call.transferInventory(player, core);
                targetItem = updateTargetItem(core2 != null);
            }
        }
    }

    @Override
    public void updateVisuals() {
    }
}
