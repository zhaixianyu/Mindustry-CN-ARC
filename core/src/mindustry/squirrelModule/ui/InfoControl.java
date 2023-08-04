package mindustry.squirrelModule.ui;

import arc.graphics.Color;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import mindustry.squirrelModule.modules.Hack;
import mindustry.squirrelModule.modules.Manager;
import mindustry.squirrelModule.modules.SMisc;

public class InfoControl {
    Seq<String> strings = new Seq<>();
    Table table = new Table();
    public static final float step = 0.1f, lineAdd = 1.5f;
    float color = 0;
    public Color theme;
    public Manager manager;

    public void toggle(boolean show) {
        table.visible = show;
    }

    public void build(Group root) {
        root.addChild(table);
        table.setFillParent(true);
        table.align(Align.topLeft);
        table.update(this::update);
        manager = new Manager(root);
        Hack.init();
    }

    public float getColor() {
        return color;
    }

    public void align(int align) {
        table.align(align);
    }

    public void add(String s) {
        strings.add(s);
    }

    public void remove(String s) {
        strings.remove(s);
    }

    private void update() {
        table.toFront();
        table.clear();
        final float[] thisColor = {color};
        theme = SMisc.color(color);
        table.table(t -> t.add(SMisc.packColor(thisColor[0]) + "VAPE").fontScale(2.5f).pad(50, 5, 0, 5)).row();
        table.table(t -> {
            for (String s : strings) {
                table.add(SMisc.packColor(thisColor[0]) + s).fontScale(1.25f).pad(0, 5, 0, 5).left().row();
                thisColor[0] = (thisColor[0] + lineAdd) % 180;
            }
        }).left();
        color = (color + step * Time.delta) % 180;
    }

    public void buildClickHUD() {
        manager.buildClickHUD();
    }
}
