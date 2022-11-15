package mindustry.entities.abilities;

import arc.util.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;

import static mindustry.Vars.tilesize;
import static mindustry.arcModule.RFuncs.abilityPro;

public class RepairFieldAbility extends Ability{
    public float amount = 1, reload = 100, range = 60;
    public Effect healEffect = Fx.heal;
    public Effect activeEffect = Fx.healWaveDynamic;
    public boolean parentizeEffects = false;

    protected float timer;
    protected boolean wasHealed = false;

    RepairFieldAbility(){}

    public RepairFieldAbility(float amount, float reload, float range){
        this.amount = amount;
        this.reload = reload;
        this.range = range;
    }

    @Override
    public String description(){
        StringBuilder des = new StringBuilder();
        des.append(abilityPro(reload/60f,"s"));
        des.append(abilityPro(range / tilesize,"格"));
        des.append(abilityPro(amount,"血"));
        return des.deleteCharAt(des.length() - 1).toString();
    }

    @Override
    public void update(Unit unit){
        timer += Time.delta;

        if(timer >= reload){
            wasHealed = false;

            Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                if(other.damaged()){
                    healEffect.at(other, parentizeEffects);
                    wasHealed = true;
                }
                other.heal(amount);
            });

            if(wasHealed){
                activeEffect.at(unit, range);
            }

            timer = 0f;
        }
    }
}
