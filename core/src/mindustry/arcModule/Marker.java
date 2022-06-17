package mindustry.arcModule;

import arc.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public class Marker{
    /** 冷却时间*/
    public static final float heatTime = 60f;
    /** 滞留时间*/
    public static final float retainTime = 1800f;

    public static final String preFixed = "<ARC";
    public static final String versionFixed = preFixed + Vars.arcVersion + ">";

    public static MarkType mark, gatherMark, attackMark, defenseMark, quesMark;

    public static Seq<MarkType> markTypes = Seq.with(
    mark = new MarkType("mark", Fx.arcMarker, Color.valueOf("eab678")),
    gatherMark = new MarkType("gather", Fx.arcGatherMarker, Color.cyan),
    attackMark = new MarkType("attack", Fx.arcAttackMarker, Color.red),
    defenseMark = new MarkType("defense", Fx.arcDefenseMarker, Color.acid),
    quesMark = new MarkType("question", Fx.arcQuesMarker, Color.pink)
    );

    public static float time;
    public static Vec2 lastPos;
    public static MarkType lastMarkTypes;
    public static boolean isLocal;

    static{
        Events.run(Trigger.update, () -> {
            time += Time.delta;
        });
    }

    public static void mark(MarkType type, float x, float y){
        mark(type, Tmp.v1.set(x, y), true);
    }

    public static void mark(MarkType type, float x, float y, boolean sendMessage){
        mark(type, Tmp.v1.set(x, y), sendMessage);
    }

    public static void mark(MarkType type, Vec2 pos){
        mark(type, pos, true);
    }

    public static void mark(MarkType type, Vec2 pos, boolean sendMessage){
        if(time < heatTime){
            Vars.ui.announce("请不要频繁标记!");
            return;
        }

        time = 0f;
        if(lastPos == null){
            lastPos = new Vec2();
        }

        lastPos.set(pos);
        lastMarkTypes = type;

        type.showEffect(pos);

        if(sendMessage){
            isLocal = true;
            type.sendMessage(pos);
        }
    }

    public static void resolveMessage(String text){
        if(isLocal){
            isLocal = false;
            return;
        }

        int preFixedIndex = text.indexOf(preFixed);

        if(preFixedIndex != -1){
            int s = text.indexOf(">", preFixedIndex) + 1;

            String typeLocalized = text.substring(text.indexOf('<', s) + 1, text.indexOf('>', s));

            MarkType markType = findLocalizedName(typeLocalized);

            if(markType == null){
                Log.err("Cannot resolve mark type from " + typeLocalized);
                return;
            }

            /* Parse position */
            String posStr = text.substring(text.indexOf('(', s + 1));

            Vec2 pos = Tmp.v1;

            try{
                pos.fromString(posStr);
            }catch(Throwable e){
                Log.err("Cannot resolve position from " + posStr);
                return;
            }

            mark(markType, pos, false);
        }

    }

    public static MarkType findType(String name){
        return markTypes.find(maskType -> maskType.name.equals(name));
    }

    public static MarkType findLocalizedName(String localizedName){
        return markTypes.find(maskType -> maskType.localizedName.equals(localizedName));
    }

    public static class MarkType{
        private final String name;

        public String localizedName;
        public String describe;

        private final Effect effect;
        public final Color color;

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
            return "[#" + color + "]" + localizedName;
        }

        public String tinyName(){
            return "[#" + color + "]" + localizedName.substring(0,1);
        }

        public void showEffect(Vec2 pos){
            effect.arcCreate(pos.x, pos.y, 0, color, null);
        }

        public void sendMessage(Vec2 pos){
            Call.sendChatMessage(versionFixed +
            "[#" + color + "]" + "<" + localizedName + ">" +
            "[white]" + ": " +
            Tmp.v1.set(World.toTile(pos.x), World.toTile(pos.y)));
        }

    }

}
