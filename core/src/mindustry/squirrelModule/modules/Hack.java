package mindustry.squirrelModule.modules;

import arc.scene.Element;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.util.Log;

import static mindustry.Vars.ui;

public class Hack {
    public static void init() {
        Manager manager = ui.infoControl.manager;
        manager.register("test", "test", new Config("test", new Element[]{new Label("test")}, new HackFunc(){
            @Override
            public void onEnable() {
                Log.info("test");
            }
        }));
        manager.register("test", "test2", new Config("test2", new Element[]{new Slider(0, 1, 0.01f, false)}, new HackFunc(){
            @Override
            public void onEnable() {
                Log.info("test2");
            }
        }));
        for (int i = 0 ; i < 10 ; i++) {
            manager.register("test2", "test" + i, new Config("test" + i, new Element[]{new Label("aaaaaaaaa"), new Button()}, new HackFunc() {
                @Override
                public void onEnable() {
                    Log.info("test");
                }
            }));
        }
    }
}
