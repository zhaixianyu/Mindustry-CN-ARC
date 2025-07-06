package mindustry.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.Draw;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;


public class BufferedItemBridge extends ItemBridge{
    public final int timerAccept = timers++;

    public float speed = 40f;
    public int bufferCapacity = 50;
    public float displayedSpeed = 11f;

    public BufferedItemBridge(String name){
        super(name);
        hasPower = false;
        hasItems = true;
        canOverdrive = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.bufferCapacity, bufferCapacity);

        //Hard to calculate, fps and overdive reliant. Movement speed taken from testing
        stats.add(Stat.itemsMoved, displayedSpeed, StatUnit.itemsSecond);
    }

    public class BufferedItemBridgeBuild extends ItemBridgeBuild{
        ItemBuffer buffer = new ItemBuffer(bufferCapacity);

        @Override
        public void updateTransport(Building other){
            if(buffer.accepts() && items.total() > 0){
                buffer.accept(items.take());
            }

            Item item = buffer.poll(speed / timeScale);
            if(timer(timerAccept, 4 / timeScale) && item != null && other.acceptItem(this, item)){
                moved = true;
                other.handleItem(this, item);
                buffer.remove();
            }
        }

        @Override
        public void doDump(){
            dump();
        }

        @Override
        public void draw(){
            super.draw();

            if(Core.settings.getInt("HiddleItemTransparency")>1){
                Item[] bufferItems = buffer.getItems();
                float[] bufferTimes = buffer.getTimes();
                Tile other = world.tile(link);

                float begx, begy, endx, endy;
                if(!linkValid(tile, other)){
                    begx = x - tilesize / 2f;
                    begy = y - tilesize / 2f;
                    endx = x + tilesize / 2f;
                    endy = y - tilesize / 2f;
                }else{
                    int i = tile.absoluteRelativeTo(other.x, other.y);
                    float ex = other.worldx() - x - Geometry.d4(i).x * tilesize / 2f,
                    ey = other.worldy() - y - Geometry.d4(i).y * tilesize / 2f;
                    float warmup = state.isEditor() ? 1f : this.warmup;
                    ex *= warmup;
                    ey *= warmup;

                    begx = x + Geometry.d4(i).x * tilesize / 2f;
                    begy = y + Geometry.d4(i).y * tilesize / 2f;
                    endx = x + ex;
                    endy = y + ey;
                }

                float loti = 0f;
                for(int idi = 0; idi < bufferItems.length; idi++){
                    if(bufferItems[idi] != null){
                        Draw.alpha((float)Core.settings.getInt("HiddleItemTransparency") / 100f);
                        Draw.rect(bufferItems[idi].uiIcon,
                        begx + ((endx - begx) / (float)bufferItems.length * Math.min(((Time.time - bufferTimes[idi]) * timeScale / speed) * bufferCapacity, bufferCapacity - loti)),
                        begy + ((endy - begy) / (float)bufferItems.length * Math.min(((Time.time - bufferTimes[idi]) * timeScale / speed) * bufferCapacity, bufferCapacity - loti)), 4f, 4f);
                    }
                    loti++;
                }
            }
        }


        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }


        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }
    }
}