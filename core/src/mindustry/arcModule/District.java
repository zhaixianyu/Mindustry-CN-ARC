package mindustry.arcModule;

import arc.*;
import arc.func.Cons;
import arc.math.geom.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Scl;
import mindustry.*;
import mindustry.arcModule.ui.dialogs.MessageDialog;
import mindustry.core.World;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import java.util.Date;

import static mindustry.Vars.*;

public class District{
    /** 冷却时间*/
    public static final float heatTime = 60f;
    /** 滞留时间*/
    public static final float retainTime = 1800f;

    public static final String preFixed = "<ARC";
    public static final String versionFixed = preFixed + Vars.arcVersion + ">";


    void showNewIconTag(Cons<String> cons){
        new Dialog(){{
            closeOnBack();
            setFillParent(true);

            cont.pane(t -> {
                resized(true, () -> {
                    t.clearChildren();
                    t.marginRight(19f);
                    t.defaults().size(48f);

                    int cols = (int)Math.min(20, Core.graphics.getWidth() / Scl.scl(52f));

                    for(ContentType ctype : defaultContentIcons){
                        t.row();
                        t.image().colspan(cols).growX().width(Float.NEGATIVE_INFINITY).height(3f).color(Pal.accent);
                        t.row();

                        int i = 0;
                        for(UnlockableContent u : content.getBy(ctype).<UnlockableContent>as()){
                            if(!u.isHidden() && u.unlockedNow() && u.hasEmoji()){
                                t.button(new TextureRegionDrawable(u.uiIcon), Styles.flati, iconMed, () -> {
                                    String out = u.emoji() + "";

                                    cons.get(out);

                                    hide();
                                });

                                if(++i % cols == 0) t.row();
                            }
                        }
                    }
                });
            });
            buttons.button("@back", Icon.left, this::hide).size(210f, 64f);
        }}.show();
    }

    public static class advDistrict{
        public District.DistictType districtType;
        public String message;
        public Date time;
        public String creator;
        public boolean selected;
        public Vec2 districtA;
        public Vec2 districtB;

        public advDistrict(District.DistictType districtType, String message, Date time, String creator, Vec2 districtA, Vec2 districtB){
            this.districtType = districtType;
            this.message = message;
            this.time = time;
            this.creator = creator;
            this.districtA = new Vec2().set(districtA);
            this.districtB = new Vec2().set(districtB);
        }

        public advDistrict(District.DistictType districtType, String message, String creator, Vec2 districtA, Vec2 districtB){
            this(districtType, message, new Date(), creator, districtA, districtB);
        }

        public advDistrict(District.DistictType districtType, String message, Vec2 districtA, Vec2 districtB){
            this(districtType, message, "null", districtA, districtB);
        }

        public String toString(){
            return  versionFixed +
                    "[规划区]" + "<" + districtType.getName() + ">" +
                    "[white]：" +
                    "(" + World.toTile(districtA.x) + "," + World.toTile(districtA.y)+")~" +
                    "(" + World.toTile(districtB.x) + "," + World.toTile(districtB.y)+")"
                    ;
        }

    }

    public static class DistictType{
        public Boolean iconName;
        public UnlockableContent districtType;
        public String districtName;

        DistictType(UnlockableContent districtType){
            this.iconName = true;
            this.districtType = districtType;
        }

        DistictType(String districtName){
            this.iconName = false;
            this.districtName = districtName;
        }

        public String getName(){
            if(iconName) return districtType.emoji();
            else
                return districtName;
        }

    }

}
