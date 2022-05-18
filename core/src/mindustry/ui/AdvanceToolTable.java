package mindustry.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.ClickListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.event.InputEvent;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.input.DesktopInput;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;
import static mindustry.Vars.world;
import static mindustry.content.UnitTypes.gamma;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.clearNonei;
import static mindustry.ui.Styles.*;


public class AdvanceToolTable extends Table {
    private boolean show = false;
    private boolean showGameMode = false,showResTool = false,showUnitStat = false;

    //unitFactory
    private UnitType spawning = UnitTypes.dagger;
    private int unitCount = 1;
    private Vec2 unitLoc = new Vec2(0, 0);
    private Team unitTeam = Team.sharded;
    private ObjectMap unitStatus = new ObjectMap();


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
                        tBox.button(Icon.waves,clearNonei, () -> {
                            unitSpawnMenu();
                        }).width(40f).tooltip("[acid]单位");
                    }).left().row();

                }


                t.row();
                t.table(mainBox -> {
                    mainBox.background(Tex.buttonEdge3);
                    mainBox.button("[red]工具箱",cleart, () -> {
                        show = !show;
                        rebuild();
                    }).width(70f);
                    mainBox.button((showGameMode?"[cyan]":"[acid]")+"规则",cleart, () -> {
                        showGameMode = !showGameMode;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showResTool?"[cyan]":"[acid]")+"资源",cleart, () -> {
                        showResTool = !showResTool;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showUnitStat?"[cyan]":"[acid]")+"单位",cleart, () -> {
                        showUnitStat = !showUnitStat;
                        rebuild();
                    }).width(50f);

                }).left();


            }).left();
        }
    }

    private void unitSpawnMenu(){
        BaseDialog unitFactory = new BaseDialog("单位工厂-ARC");

        Table table = unitFactory.cont;
        Runnable[] rebuild = {null};
        rebuild[0] = () -> {
            table.clear();
            /* Unit */
            table.label(() -> spawning.localizedName);
            table.row();

            table.pane(list -> {
                int i = 0;
                for (UnitType units : content.units()) {
                    if (i++ % 8 == 0) list.row();
                    list.button(units.emoji(), () -> {
                        spawning = units;
                    }).size(50f);
                }
            }).top().center();

            table.row();

            Table r = table.table().center().bottom().get();
            r.add("数量：");
            r.field("" + unitCount, text -> {
                unitCount = Integer.parseInt(text);
            }).maxTextLength(4).valid(value -> Strings.canParsePositiveInt(value)).get();

            table.row();

            table.table(t -> {
                    t.add("坐标：x= ");
                    t.field(String.valueOf((int) unitLoc.x), text -> {
                        unitLoc.x = Integer.parseInt(text);
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveFloat(value)).get();
                    t.add("  ,y= ");
                    t.field(String.valueOf((int) unitLoc.y), text -> {
                        unitLoc.y = Integer.parseInt(text);
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveFloat(value)).get();
                    t.button(gamma.emoji(),()->{
                        unitLoc.x = ui.chatfrag.getArcMarkerX() / tilesize;
                        unitLoc.y = ui.chatfrag.getArcMarkerY() / tilesize;
                        rebuild[0].run();
                    }).tooltip("选择上个标记点：" + (int)  (ui.chatfrag.getArcMarkerX() / tilesize) + "," + (int) (ui.chatfrag.getArcMarkerY() / tilesize)).height(50f);
                }
            );

            table.row();

            table.table(t -> {
                    t.add("队伍：");
                    t.field(String.valueOf(unitTeam.id), text -> {
                        unitTeam = Team.get(Integer.parseInt(text));
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveInt(value)).get();
                    for(Team team:Team.baseTeams){
                        t.button(String.valueOf(team.id),()->{unitTeam = team;rebuild[0].run();}).size(30,30).color(team.color);
                    }
                    t.button("更多", cleart ,() -> {
                        BaseDialog selectTeamDialog = new BaseDialog("队伍选择器");
                        Table selectTeam = new Table().top();
                        selectTeamDialog.cont.pane(td->{
                                    int j = 0;
                                    for(Team team : Team.all){
                                        ImageButton button = new ImageButton(Tex.whiteui, Styles.clearTogglei);
                                        button.getStyle().imageUpColor = team.color;
                                        button.margin(10f);
                                        button.resizeImage(40f);
                                        button.clicked(() -> {
                                            Call.setPlayerTeamEditor(player, team);selectTeamDialog.hide();rebuild[0].run();});
                                        button.update(() -> button.setChecked(player.team() == team));
                                        td.add(button);
                                        j++;
                                        if(j==5) {td.row();td.add("队伍："+j+"~"+(j+10));}
                                        else if((j-5)%10==0) {td.row();td.add("队伍："+j+"~"+(j+10));}
                                    }
                                }
                        );

                        selectTeamDialog.add(selectTeam).center();
                        selectTeamDialog.row();

                        selectTeamDialog.addCloseButton();
                        selectTeamDialog.show();
                    }).center().width(50f).row();
                }
            );

            table.row();

            table.button("[cyan]生成！", Icon.modeAttack, () -> {
                for (var n = 0; n < unitCount; n++) {
                    Tmp.v1.rnd(Mathf.random(5f * Vars.tilesize));
                    var unit = spawning.create(unitTeam);
                    unit.set(unitLoc.x * tilesize + Tmp.v1.x, unitLoc.y * tilesize + Tmp.v1.y);
                    unit.add();
                }
                if (control.input instanceof DesktopInput) {
                    ((DesktopInput) control.input).panning = true;
                }
                Core.camera.position.set(unitLoc.x * tilesize,unitLoc.y * tilesize);
                unitFactory.closeOnBack();
            }).fillX();
        };
        rebuild[0].run();

        unitFactory.addCloseButton();
        unitFactory.show();
    }


    /* Buttons */
        /*
        dialog.buttons.button(new TextureRegionDrawable(StatusEffects.burning.uiIcon), 40, () => {
                status.build().show();
	});

	const teamRect = extend(TextureRegionDrawable, Tex.whiteui, {});
        teamRect.tint.set(team.color);
        dialog.buttons.button("设置队伍", teamRect, 40, () => {
                ui.select("设置队伍", Team.baseTeams, t => {
                        team = t;
        teamRect.tint.set(team.color);
		}, (i, t) => "[#" + t.color + "]" + t);
	});

    const sButton = new ImageButton(statusicon.icon(Cicon.full), Styles.logici);
        let bs = sButton.style;
        bs.down = Styles.flatDown;
        bs.over = Styles.flatOver;
        bs.imageDisabledColor = Color.gray;
        bs.imageUpColor = statusicon.color;
        bs.disabled = Tex.whiteui.tint(0.625, 0, 0, 0.8);
        */
    /* Set clicky */
}