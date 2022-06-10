package mindustry.arcModule;

import arc.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;

public class Marker{
    public static final float heatTime = 35f;

    public static MarkType mark, gatherMark, attackMark, defenseMark, quesMark;

    public static Seq<MarkType> markTypes = Seq.with(
    mark = new MarkType("mark", Fx.arcMarker, Color.valueOf("eab678")),
    gatherMark = new MarkType("gather", Fx.arcGatherMarker, Color.cyan),
    attackMark = new MarkType("attack", Fx.arcAttackMarker, Color.red),
    defenseMark = new MarkType("defense", Fx.arcDefenseMarker, Color.acid),
    quesMark = new MarkType("question", Fx.arcQuesMarker, Color.pink)
    );

    public float time;

    public Marker(){
        Events.run(Trigger.update, () -> {
            time = Math.min(heatTime, time + Time.delta);
        });
    }

    public void mark(MarkType type, float x, float y){
        mark(type, Tmp.v1.set(x, y));
    }

    public void mark(MarkType type, Vec2 pos){
        if(time != heatTime){
            Vars.ui.announce("请不要连续使用标记");
            return;
        }

        time = 0f;
        type.showEffect(pos);
    }

    public void resolveMessage(String message){
        String markType = "";

        if(message.contains("<ARC")){
            int x = 0, y = 0;
            try{
                int strLength = message.length();
                int stopindex = 0;
                for(int i = 0; i < strLength; i++){
                    if(message.charAt(i) == '>'){
                        markType = message.substring(i + 1, i + 3).trim();
                    }
                    if(message.charAt(i) == '('){
                        stopindex = i;
                    }
                    if(message.charAt(i) == ',' && stopindex > 0){
                        x = Integer.parseInt(message.substring(stopindex + 1, i).trim());
                        y = Integer.parseInt(message.substring(i + 1, strLength - 1).trim());
                        break;
                    }
                }
            }catch(Exception e){
                Log.err(e);
                return;
            }
            mark(findType(markType), x, y);

        }else if(message.contains("发起集合")){
            int strLength = message.length();
            int x = 0;
            int y = 0;
            int initindex = 0;
            int interval = 0;
            try{
                for(int i = 0; i < strLength; i++){
                    if(message.charAt(i) == '('){
                        initindex = i;
                    }
                    if(initindex > 0){
                        if(message.charAt(i) == ','){
                            interval = i;
                        }
                        if(message.charAt(i) == ')'){
                            x = Integer.parseInt(message.substring(initindex + 6, interval).trim());
                            y = Integer.parseInt(message.substring(interval + 1, i - 7).trim());
                            break;
                        }
                    }
                }
            }catch(Exception e){
                Log.err(e);
            }

            mark(gatherMark, x, y);
        }

    }

    public MarkType findType(String name){
        return markTypes.find(maskType -> maskType.name.equals(name));
    }

    public static class MarkType{
        private final String name;

        public String localizedName;
        public String describe;

        private final Effect effect;
        private final Color color;

        public MarkType(String name, Effect effect){
            this(name, effect, Color.white);
        }

        public MarkType(String name, Effect effect, Color color){
            this.name = name;
            this.effect = effect;
            this.color = color;

            localizedName = Core.bundle.get("marker." + name + ".name", "unknown");
            describe = Core.bundle.get("marker." + name + ".description", "unknown");
        }

        public String shortName(){
            return "[#" + color + "]" + localizedName.substring(0,1);
        }

        public void showEffect(Vec2 pos){
            effect.arcCreate(pos.x, pos.y, 0, color, null);
        }

    }

}
