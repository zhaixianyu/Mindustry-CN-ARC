package mindustry.ai.types;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.entities.units.*;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;

import static mindustry.Vars.*;
import static mindustry.Vars.player;

public class arcMinerAI extends AIController{
    private Seq<Block> oreList = content.blocks().select(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null);
    private Seq<Block> oreWallList  = content.blocks().select(b -> ((b instanceof Floor f && f.wallOre)|| b instanceof StaticWall) && b.itemDrop != null);
    //private Seq<Block> selOreList = new Seq<>();
    //private Seq<Block> selOreWallList  = new Seq<>();
    private Seq<Block> selOreList = oreList;
    private Seq<Block> selOreWallList  = oreWallList;

    public boolean mining = true;
    public Item targetItem;
    public Tile ore;

    @Override
    public void updateMovement(){
        Building core = unit.closestCore();

        if(!(unit.canMine()) || core == null) return;

        if(unit.mineTile != null && !unit.mineTile.within(unit, unit.type.mineRange)){
            unit.mineTile(null);
        }

        if(mining){
            if(timer.get(timerTarget2, 60 * 4) || targetItem == null){
                if(unit.type.mineFloor && !selOreList.isEmpty()) targetItem = selOreList.select(t->t.itemDrop.hardness<=unit.type.mineTier).sort(i -> core.items.get(i.itemDrop)).first().itemDrop;
                else if ((unit.type.mineWalls && !selOreWallList.isEmpty())) targetItem = selOreWallList.select(t->t.itemDrop.hardness<=unit.type.mineTier).sort(i -> core.items.get(i.itemDrop)).first().itemDrop;
                else targetItem = null;
            }

            //core full of the target item, do nothing
            if(targetItem != null && core.acceptStack(targetItem, 1, unit) == 0){
                unit.clearItem();
                unit.mineTile = null;
                return;
            }

            //if inventory is full, drop it off.
            if(unit.stack.amount >= unit.type.itemCapacity || (targetItem != null && !unit.acceptsItem(targetItem))){
                mining = false;
            }else{
                if(timer.get(timerTarget3, 60) && targetItem != null){
                    ore = unit.type.mineFloor? indexer.findClosestOre(unit, targetItem) : indexer.findClosestWallOre(unit, targetItem);
                }

                if(ore != null){
                    moveTo(ore, unit.type.mineRange / 2f, 20f);

                    if(((ore.block() == Blocks.air && unit.type.mineFloor) || (unit.type.mineWalls && ore.block()!=Blocks.air)) && unit.within(ore, unit.type.mineRange)){
                        unit.mineTile = ore;
                    }

                    if((ore.block() != Blocks.air && unit.type.mineFloor) || (unit.type.mineWalls && ore.block()==Blocks.air)){
                        mining = false;
                    }
                }
            }
        }else{
            unit.mineTile = null;

            if(unit.stack.amount == 0){
                mining = true;
                return;
            }

            if(unit.within(core, unit.type.range)){

                if(core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0){
                        Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core);
                    }
                Call.dropItem(player.angleTo(player.x, player.y));
                mining = true;
            }

            circle(core, unit.type.range / 1.8f);
        }
    }


}
