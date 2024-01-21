package mindustry.arcModule.ui.scratch.block;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.struct.Seq;
import mindustry.arcModule.ui.scratch.BlockInfo;
import mindustry.arcModule.ui.scratch.ScratchTable;
import mindustry.arcModule.ui.scratch.ScratchType;
import mindustry.arcModule.ui.scratch.block.fork.*;

@SuppressWarnings("unused")
public class ForkBlock extends ScratchBlock {
    int id = 0;
    public ForkBlock(ScratchType type, Color color, ForkInfo info) {
        this(type, color, info, false);
    }

    public ForkBlock(ScratchType type, Color color, ForkInfo info, boolean drag) {
        super(type, color, info, drag, false);
        marginLeft(15);
    }

    public void header(BlockInfo info) {
        ForkHeader e = new ForkHeader(info, elemColor, id++);
        e.cell(add(e));
    }

    public void inner() {
        ForkInner e = new ForkInner(ScratchType.none, elemColor, new BlockInfo(), id++);
        e.cell(add(e));
    }

    public void middle() {
        ForkMiddle e = new ForkMiddle(elemColor, id++);
        e.cell(add(e));
    }

    public void footer() {
        ForkFooter e = new ForkFooter(ScratchType.none, elemColor, new BlockInfo(), id++);
        e.cell(add(e));
    }

    @Override
    public ForkBlock copy(boolean drag) {
        ForkBlock sb = new ForkBlock(type, elemColor, (ForkInfo) info, true);
        for (int i = 0; i < elements.size; i++) {
            Element child = elements.get(i);
            if (child instanceof ForkComponent f) {
                f.copyTo((ForkComponent) sb.elements.get(i), drag);
            }
        }
        layout();
        return sb;
    }

    @Override
    public void cell(Cell<ScratchTable> c) {
        super.cell(c);
        c.pad(0);
    }

    @Override
    public void drawBackground() {
        Draw.color(elemColor);
        Fill.crect(x, y, 15, height - 7);
    }

    @Override
    public void drawChildren() {
        super.drawChildren();
    }

    @Override
    public void ensureParent() {
        elements.each(e -> {
            if (e instanceof ForkHasChild f) f.ensureParent();
        });
    }

    public static class ForkInfo extends BlockInfo {
        Cons<ForkBlock> builder;
        ValSupp supp = s -> null;

        public ForkInfo(Cons<ForkBlock> builder) {
            this.builder = builder;
        }

        public ForkInfo(Cons<ForkBlock> builder, BlockInfo.ValSupp supp) {
            this.builder = builder;
            this.supp = supp;
        }

        public void build(ForkBlock block) {
            builder.get(block);
        }

        public Object getValue(Seq<Element> elements) {
            return supp.get(elements);
        }

        @Override
        public void build(ScratchBlock block) {
            builder.get((ForkBlock) block);
        }
    }
}
