package mindustry.squirrelModule.modules;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class Hack {
    public static boolean immediatelyTurn = false;

    public static void init() {
        if (!settings.getBool("squirrel")) System.exit(0);
        Manager manager = ui.infoControl.manager;
        manager.register("杂项", "noArcPacket", new Config("停发版本", null, new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                settings.put("arcAnonymity", enabled);
            }
        }));
        manager.register("移动", "immediatelyTurn", new Config("瞬间转向", null, new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                immediatelyTurn = enabled;
            }
        }));
    }
}
