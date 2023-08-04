package mindustry.squirrelModule.modules;

import arc.Events;
import arc.func.Cons;
import mindustry.game.EventType;

import static arc.Core.settings;
import static mindustry.Vars.state;
import static mindustry.Vars.ui;

public class Hack {
    public static boolean noFog;

    public static boolean randomUUID, randomUSID;
    public static boolean simMobile;
    public static boolean immediatelyTurn;
    public static boolean ignoreTurn;

    public static void init() {
        if (!settings.getBool("squirrel")) System.exit(0);
        Manager manager = ui.infoControl.manager;

        manager.register("显示", "noFog", new Config("强制透雾", null, changed(e -> noFog = e)));
        manager.register("显示", "hideHUD", new Config("隐藏HUD", null, changed(e -> ui.infoControl.toggle(!e))));
        Events.run(EventType.Trigger.draw, () -> {
            if (noFog) state.rules.fog = false;
        });

        manager.register("多人", "randomUUID", new Config("随机UUID", null, changed(e -> randomUUID = e)));
        manager.register("多人", "randomUSID", new Config("随机USID", null, changed(e -> randomUSID = e)));
        manager.register("多人", "simMobile", new Config("模拟手机", null, changed(e -> simMobile = e)));

        manager.register("移动", "immediatelyTurn", new Config("瞬间转向", null, changed(e -> immediatelyTurn = e)));
        manager.register("移动", "ignoreTurn", new Config("无视旋转", null, changed(e -> ignoreTurn = e)));

        manager.register("杂项", "noArcPacket", new Config("停发版本", null, changed(e -> settings.put("arcAnonymity", e))));
    }

    public static HackFunc changed(Cons<Boolean> func) {
        return new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                func.get(enabled);
            }
        };
    }
}
