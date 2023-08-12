package mindustry.squirrelModule.modules.tools;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.arcModule.ui.window.Window;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.ui.Styles;

import static mindustry.Vars.control;
import static mindustry.Vars.ui;

public class UnitSelector extends Selector{
    Boolf<Unit> filter = u -> true;
    Seq<Unit> units = new Seq<>();
    public void show(Cons<Entityc> cb) {
        Window w = Vars.ui.WindowManager.createWindow();
        w.setTitle("单位选择器");
        Table t = new Table();
        w.setBody(t);
        w.setIcon(Icon.units.getRegion());
        t.button("选择玩家单位", () -> {
            w.remove();
            cb.get(Vars.player.unit());
        }).growX().height(32).row();
        Groups.unit.copy(units);

        //units.sort(Structs.comps(Structs.comparing(u.)));
        t.pane(t1 -> units.each(filter, u -> {
            Button[] button = {null};
            button[0] = t1.button(b -> {
                ClickListener listener = new ClickListener();
                Table iconTable = new Table() {
                    @Override
                    public void draw(){
                        super.draw();
                        Draw.colorMul(u.team.color, listener.isOver() ? 1.3f : 1f);
                        Draw.alpha(parentAlpha);
                        Lines.stroke(Scl.scl(4f));
                        Lines.rect(x, y, width, height);
                        Draw.reset();
                    }
                };
                iconTable.addListener(listener);
                iconTable.addListener(new HandCursorListener());
                iconTable.touchable = Touchable.enabled;
                iconTable.tapped(() -> {
                    if(!u.dead) {
                        Core.camera.position.set(u);
                        if(control.input instanceof DesktopInput input){
                            input.panning = true;
                        }
                    }
                });
                iconTable.add(new Image(u.icon()).setScaling(Scaling.bounded)).grow();
                b.clearChildren();
                b.add(iconTable).size(32);
                b.label(() -> u.type.localizedName + " #"  + u.id).growX();
                b.setDisabled(() -> u.dead);
            }, Styles.clearNonei, () -> {
                if (button[0].childrenPressed()) return;
                w.remove();
                cb.get(u);
            }).height(32).growX().get();
            t1.row();
        })).grow().get().setScrollingDisabledX(true);
    }

    public UnitSelector filter(Boolf<Unit> filter) {
        this.filter = filter;
        return this;
    }
}
