package mindustry.squirrelModule.modules;

import arc.Core;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.squirrelModule.ui.ControlTable;
import mindustry.ui.Styles;

public class Manager {
    public WidgetGroup controlGroup = new WidgetGroup();
    ControlTable control;
    ObjectMap<String, Seq<Config>> list = new ObjectMap<>();
    ObjectMap<String, Config> flatList = new ObjectMap<>();

    public Manager(Group root) {
        control = new ControlTable(list);
        root.addChild(control);
        Core.scene.add(controlGroup);
        controlGroup.setFillParent(true);
        controlGroup.touchable = Touchable.childrenOnly;
        controlGroup.visible = false;
        controlGroup.addChild(new Table(t -> {
            t.setFillParent(true);
            t.setBackground(Styles.black3);
            t.update(t::toBack);
        }));
    }

    public Config getConfig(String name) {
        return flatList.get(name);
    }

    public void register(String type, String name, Config conf) {
        conf.internalName = name;
        Seq<Config> s = list.get(type, new Seq<>());
        list.put(type, s);
        s.add(conf);
        flatList.put(name, conf);
        if (Core.settings.getBool(name + "e")) {
            conf.func.onEnable();
            conf.func.onChanged(true);
            conf.enabled = true;
            Vars.ui.infoControl.add(conf.displayName);
        } else {
            conf.func.onChanged(false);
        }
    }

    public void buildClickHUD() {
        control.buildClickHUD(controlGroup);
    }
}
