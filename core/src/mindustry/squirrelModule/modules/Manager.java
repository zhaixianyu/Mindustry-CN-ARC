package mindustry.squirrelModule.modules;

import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import mindustry.squirrelModule.ui.ControlTable;

public class Manager {
    ControlTable control;
    ObjectMap<String, ObjectMap<String, Config>> list = new ObjectMap<>();
    public Manager(Group root) {
        control = new ControlTable(list);
        root.addChild(control);
    }

    public void register(String type, String name, Config conf) {
        ObjectMap<String, Config> o = list.get(type, new ObjectMap<>());
        list.put(type, o);
        o.put(name, conf);
    }

    public void buildClickHUD() {
        control.buildClickHUD();
    }
}
