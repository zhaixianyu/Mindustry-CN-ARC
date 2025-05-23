package mindustry.world.blocks.heat;

import arc.math.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.arcModule.NumberFormat;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

public class HeatProducer extends GenericCrafter{
    public float heatOutput = 10f;
    public float warmupRate = 0.15f;

    public HeatProducer(String name){
        super(name);

        drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;
        //it doesn't count as a standard crafter
        flags = EnumSet.of();
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, heatOutput, StatUnit.heatUnits);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("heat", (HeatProducerBuild entity) -> new Bar(()-> NumberFormat.formatPercent("热量", entity.heat , heatOutput), () -> Pal.lightOrange, () -> entity.heat / heatOutput));
    }

    public class HeatProducerBuild extends GenericCrafterBuild implements HeatBlock{
        public float heat;

        @Override
        public void updateTile(){
            super.updateTile();

            //heat approaches target at the same speed regardless of efficiency
            heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * delta());
        }

        @Override
        public float heatFrac(){
            return heat / heatOutput;
        }

        @Override
        public float heat(){
            return heat;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
        }
    }
}
