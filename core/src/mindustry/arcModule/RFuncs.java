package mindustry.arcModule;

import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class RFuncs{
    
    public static String arcShareWaveInfo(int waves){
        if(!state.rules.waves) return " ";
        StringBuilder builder = new StringBuilder();
        builder.append(arcVersionPrefix);
        builder.append("标记了第").append(waves).append("波");
        if(waves < state.wave){
            builder.append("。");
        }else{
            if(waves > state.wave){
                builder.append("，还有").append(waves - state.wave).append("波");
            }
            int timer = (int)(state.wavetime + (waves - state.wave) * state.rules.waveSpacing);
            builder.append("[").append(fixedTime(timer)).append("]。");
        }

        builder.append(arcWaveInfo(waves));
        return builder.toString();
    }

    public static String arcWaveInfo(int waves){
        StringBuilder builder = new StringBuilder();
        if(state.rules.attackMode){
            int sum = Math.max(state.teams.present.sum(t -> t.team != player.team() ? t.cores.size : 0), 1) + Vars.spawner.countSpawns();
            builder.append("其包含(×").append(sum).append(")");
            builder.append("包含(×").append(sum).append(")");
        }else{
            builder.append("其包含(×").append(Vars.spawner.countSpawns()).append("):");
            builder.append("包含(×").append(Vars.spawner.countSpawns()).append("):");
        }
        for(SpawnGroup group : state.rules.spawns){
            if(group.getSpawned(waves - 1) > 0){
                builder.append((char)Fonts.getUnicode(group.type.name)).append("(");
                if(group.effect != StatusEffects.invincible && group.effect != StatusEffects.none && group.effect != null){
                    builder.append((char)Fonts.getUnicode(group.effect.name)).append("|");
                }
                if(group.getShield(waves - 1) > 0){
                    builder.append(UI.whiteformatAmount((int)group.getShield(waves - 1))).append("|");
                }
                builder.append(group.getSpawned(waves - 1)).append(")");
            }
        }
        return builder.toString();
    }

    public static String fixedTime(int timer){
        StringBuilder str = new StringBuilder();
        int m = timer / 60 / 60;
        int s = timer / 60 % 60;
        int ms = timer % 60;
        if(m > 0){
            str.append(m).append(": ");
            if(s < 10){
                str.append("0");
            }
            str.append(s).append("min");
        }else{
            str.append(s).append(".").append(ms).append('s');
        }
        return str.toString();
    }

}
