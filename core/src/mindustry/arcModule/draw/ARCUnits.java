package mindustry.arcModule.draw;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Flyingc;
import mindustry.type.UnitType;

import static mindustry.Vars.control;

public class ARCUnits {
    public static float unitTrans = 1f;
    public static float unitDrawMinHealth = 1f, unitBarDrawMinHealth = 1f;

    public static float unitWeaponRangeAlpha = 1f, unitAlertRange = 1f;

    public static boolean selectedUnitsFlyer = false, selectedUnitsLand = false;

    private boolean canHitPlayer = false, canHitCommand = false;

    static {
        // 减少性能开销
        Events.run(EventType.Trigger.update, () -> {
            unitTrans = Core.settings.getInt("unitTransparency") / 100f;
            unitDrawMinHealth = Core.settings.getInt("unitDrawMinHealth");
            unitBarDrawMinHealth = Core.settings.getInt("unitBarDrawMinHealth");

            unitWeaponRangeAlpha = Core.settings.getInt("unitweapon_range") / 100f;
            unitAlertRange = Core.settings.getInt("unitAlertRange");

            selectedUnitsFlyer = control.input.selectedUnits.contains(Flyingc::isFlying);
            selectedUnitsLand = control.input.selectedUnits.contains(unit -> !unit.isFlying());
        });
    }

    static void arcDrawUnit(UnitType unit){
        //单位武器射程

    }

}
