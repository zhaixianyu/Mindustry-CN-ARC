package mindustry.arcModule.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.arcModule.Marker;
import mindustry.content.Fx;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.logic.LCanvas;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.state;
import static mindustry.ui.Styles.flatToggleMenut;

public class MessageDialog extends BaseDialog {
    /**选择的第一个|最后一个记录*/
    private int msgInit,msgFinal;

    private static arcMsgType normal,markLoc,markWave,serverTips,serverMsg,serverToast,logic;

    public static Seq<arcMsgType> msgTypes = Seq.with(
        normal = new arcMsgType(""),
        markLoc = new arcMsgType("标记","坐标"),
        markWave = new arcMsgType("标记","波次"),
        serverTips = new arcMsgType("服务器","小贴士"),
        serverMsg =  new arcMsgType("服务器","信息"),
        serverToast =  new arcMsgType("服务器","通报"),
        logic =  new arcMsgType("逻辑")
    );

    public static Seq<advanceMsg> msgList = new Seq<>();

    public MessageDialog() {
        super("arc-信息交互中心");

        table(t->{
            t.add("信息筛选").row();
            for(int i=0;i<msgTypes.size;i++){
                if((i-1) % 5 == 0)  t.row();
                int finalI = i;
                t.button(msgTypes.get(i).type,flatToggleMenut,() -> msgTypes.get(finalI).showChatFrag = !msgTypes.get(finalI).showChatFrag).checked(msgTypes.get(finalI).showChatFrag);
            }
        });

        build();

        buttons.defaults().size(160f, 64f);
        buttons.button("@back", Icon.left, this::hide).name("back");
        buttons.button("导出", Icon.left, ()-> exportMsg()).name("导出聊天记录");
    }

    void build(){
        clear();
        if (msgList.size == 0) return;
        for(int i=0;i<msgList.size;i++){
            int finalI = i;
            table(Tex.whiteui, t -> {
                advanceMsg thisMsg = msgList.get(finalI);
                t.color.set(thisMsg.msgType.color);

                t.margin(6f);

                t.add(thisMsg.msgType.typeName).style(Styles.outlineLabel).color(color).padRight(8);
                t.add().growX();

                t.button(Icon.copy, Styles.logici, () ->Core.app.setClipboardText(thisMsg.message)).size(24f).padRight(6).get();

                t.button(Icon.cancel, Styles.logici, () -> {
                    msgList.remove(finalI);
                    build();
                }).size(24f);
                t.row();
                t.field(thisMsg.message,text->{});
            }).growX().height(38);
        }
    }

    void addMsg(String msg){


    }

    void exportMsg(){

    }

    public static class advanceMsg{
        public arcMsgType msgType;
        public String message;
        public float time;
        public String sender;

        public advanceMsg(arcMsgType msgType, String message, float time, String sender){
            this.msgType = msgType;
            this.message = message;
            this.time = time;
            this.sender = sender;
        }

        public advanceMsg(arcMsgType msgType, String message){
            this(msgType,message,Time.time);
        }

        public advanceMsg(arcMsgType msgType, String message, float time){
            this(msgType,message,time,"null");
        }
    }

    public static class arcMsgType{
        public String typeName;
        public String type;
        public String subClass;
        public Boolean showChatFrag = true;
        public Color color;

        arcMsgType(String type,String subClass){
            this.type = type;
            this.subClass = subClass;
            this.typeName = type + "~" + subClass;
        }

        arcMsgType(String type){
            this.type = type;
            this.subClass = "";
            this.typeName = type;
        }

    }
}