package mindustry.ui;

import arc.graphics.Color;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.game.Teams;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.type.UnitType;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.clearNonei;
import static mindustry.ui.Styles.*;


public class AdvanceToolTable extends Table {
    private boolean show = false;
    private boolean showGameMode = false,showResTool = false,showUnitStat = false;


    private TextButton.TextButtonStyle textStyle;

    public AdvanceToolTable() {
        textStyle = new TextButton.TextButtonStyle(){{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        rebuild();
    }

    void rebuild(){
        clear();
        if(!show) {
            table(t -> {
                t.background(Tex.buttonEdge3);
                t.button("[cyan]工具箱", cleart, () -> {
                    show = !show;
                    rebuild();
                }).left().width(70).expandX();

            }).left();
        }
        else{
            table(t -> {
                if (showGameMode){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("规则：").left();
                        tBox.button("沙盒",cleart, () -> {
                            state.rules.editor = !state.rules.editor;
                        }).width(40f);
                    }).left().row();
                }
                if (showResTool){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("资源：").left();
                        tBox.button(Items.copper.emoji()+"[acid]+",cleart, () -> {
                            for(Item item :content.items())  player.core().items.set(item,player.core().storageCapacity);
                        }).width(40f);
                        tBox.button(Items.copper.emoji()+"[red]-",cleart, () -> {
                            for(Item item :content.items())  player.core().items.set(item,0);
                        }).width(40f);
                    }).left().row();
                }
                if(showUnitStat){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("单位：").left();
                        tBox.button(UnitTypes.gamma.emoji()+"[acid]+",cleart, () -> {
                            player.unit().type().spawn(player.team(),player.x,player.y);
                        }).width(40f).tooltip("[acid]克隆");
                        tBox.button(UnitTypes.gamma.emoji()+"[red]×",cleart, () -> {
                            player.unit().kill();
                        }).width(40f).tooltip("[red]自杀");
                    }).left().row();

                }


                t.row();
                t.table(mainBox -> {
                    mainBox.background(Tex.buttonEdge3);
                    mainBox.button("[red]工具箱",cleart, () -> {
                        show = !show;
                        rebuild();
                    }).width(70f);
                    mainBox.button((showGameMode?"[acid]":"[cyan]")+"规则",cleart, () -> {
                        showGameMode = !showGameMode;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showResTool?"[acid]":"[cyan]")+"资源",cleart, () -> {
                        showResTool = !showResTool;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showUnitStat?"[acid]":"[cyan]")+"单位",cleart, () -> {
                        showUnitStat = !showUnitStat;
                        rebuild();
                    }).width(50f);

                }).left();


            }).left();
        }

    }

}