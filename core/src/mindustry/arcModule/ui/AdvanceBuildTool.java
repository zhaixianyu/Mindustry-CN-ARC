package mindustry.arcModule.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.flatDown;
import static mindustry.ui.Styles.flatOver;

public class AdvanceBuildTool extends Table {
    private boolean expandList = false;

    private TextButton.TextButtonStyle textStyle, NCtextStyle;

    private boolean allBlock = false;

    public boolean writeInProperty = false;

    private Block original = Blocks.conveyor, newBlock = Blocks.titaniumConveyor;

    public AdvanceBuildTool() {
        textStyle = new TextButton.TextButtonStyle() {{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            checked = flatDown;
        }};

        NCtextStyle = new TextButton.TextButtonStyle() {{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        rebuild();
        right();
    }

    void rebuild() {
        clearChildren();

        if (expandList) {
            /*
            table(t->{
                t.check("\uE869",writeInProperty, tp -> writeInProperty = tp);
            }).row();*/
            table(t->{
                t.setBackground(Styles.black6);
                t.button("R", NCtextStyle, this::replaceBlock).size(30f);
                t.button(replaceBlockName(), this::replaceBlockSetting).width(100f).height(30f);
            });
        }

        button(Blocks.buildTower.emoji(), textStyle, () -> {
            expandList = !expandList;
            rebuild();
        }).size(40f).fillY();
    }

    void replaceBlockSetting(){
        Dialog dialog = new Dialog("方块替换器");
        dialog.cont.table(t->{
            t.table(tt-> tt.label(this::replaceBlockName)).row();
            t.image().color(getThemeColor()).fillX().row();
            t.table(tt->{
                replaceBlockGroup(tt, Blocks.conveyor, Blocks.titaniumConveyor);
                replaceBlockGroup(tt, Blocks.conveyor, Blocks.duct);
                replaceBlockGroup(tt, Blocks.conduit, Blocks.pulseConduit);
                replaceBlockGroup(tt, Blocks.conduit, Blocks.reinforcedConduit);
            }).row();
            t.image().color(getThemeColor()).fillX().row();
        });
        dialog.addCloseButton();

        dialog.show();
    }

    void replaceBlockGroup(Table t, Block ori, Block re){
        if (state.rules.bannedBlocks.contains(re)) return;
        if (!indexer.isBlockPresent(ori)) return;
        t.button(replaceBlockName(ori,re), ()->{
            original = ori;
            newBlock = re;
        }).size(30f);
    }
    String replaceBlockName(){
        return replaceBlockName(original, newBlock);
    }

    String replaceBlockName(Block ori, Block re){
        return ori.emoji() + "\uE803" + re.emoji();
    }

    void replaceBlock(){
        replaceBlock(original, newBlock, buildingRange);
    }

    void replaceBlock(Block ori, Block re, Float range){
        indexer.eachBlock(player.team(), player.x, player.y, range, building -> building.block() == ori,
                building -> player.unit().addBuild(new BuildPlan(building.tile.x, building.tile.y, building.rotation, re, building.config())));
    }

}
