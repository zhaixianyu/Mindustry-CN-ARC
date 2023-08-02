package mindustry.squirrelModule.ui;

import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;

public class InfoControl {
    Seq<String> strings = new Seq<>();
    Table table = new Table();
    float step = 1.8f, rotSpeed = 0.4f;
    float colorStart = 0, sum = 0;
    public void build(Group root) {
        root.addChild(table);
        table.setFillParent(true);
        table.align(Align.topRight);
        table.update(this::update);
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
        sum = colorStart;
        for (String s : strings) {
            table.add(SMisc.color(s, step, sum)).row();
            sum += rotSpeed;
        }
        colorStart = (colorStart + rotSpeed) % 180;
    }
}
