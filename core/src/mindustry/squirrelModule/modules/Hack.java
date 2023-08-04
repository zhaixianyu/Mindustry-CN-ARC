package mindustry.squirrelModule.modules;

import arc.scene.Element;
import arc.scene.ui.Label;
import arc.util.Log;

import static mindustry.Vars.ui;

public class Hack {
    public static void init() {
        Manager manager = ui.infoControl.manager;
        manager.register("test", "test", new Config(new Element[]{new Label("test")}, new HackFunc(){
            @Override
            public void onEnable() {
                Log.info("test");
            }
        }));
    }
}
