package mindustry.arcModule.ui.dialogs;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.arcModule.Marker;
import mindustry.core.World;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.logic.LCanvas;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.storage.CoreBlock;

import java.text.SimpleDateFormat;
import java.util.Date;

import static mindustry.Vars.*;
import static mindustry.ui.Styles.flatToggleMenut;
import static mindustry.ui.Styles.nodeArea;

public class MessageDialog extends BaseDialog {
    /**选择的第一个|最后一个记录*/
    private int msgInit,msgFinal;
    /**存储的所有事件记录*/
    public static Seq<advanceMsg> msgList = new Seq<>();

    private Table historyTable;
    private Boolean fieldMode = false;

    public MessageDialog() {
        super("arc-中央监控室");

        cont.pane(t->{
            historyTable = t;
        }).growX().scrollX(false);

        addCloseButton();
        buttons.button("模式切换",()->{fieldMode = !fieldMode;build();});
        buttons.button("清空", ()-> clearMsg());
        buttons.button("导出", Icon.upload, ()-> exportMsg()).name("导出聊天记录");

        init();
        shown(this::build);
        onResize(this::build);

        Events.on(EventType.WorldLoadEvent.class, e->{
            addMsg(new MessageDialog.advanceMsg(arcMsgType.eventWorldLoad,"载入地图"));
        });

        Events.on(EventType.WaveEvent.class, e->{
            if(state.wavetime<30f) return;
            addMsg(new MessageDialog.advanceMsg(arcMsgType.eventWave,"波次发生:"+state.wave));
        });

        Events.on(EventType.BlockDestroyEvent.class, e->{
            if(e.tile.build instanceof CoreBlock.CoreBuild)
            addMsg(new MessageDialog.advanceMsg(arcMsgType.eventCoreDestory,"核心摧毁:"+"(" + World.toTile(e.tile.x) + "," + World.toTile(e.tile.y)+")"));
        });
    }

    void init(){

    }

    void build(){
        historyTable.clear();
        historyTable.setWidth(800f);
        if (msgList.size == 0) return;
        for(int i=msgList.size - 1;i>=0;i--){
            int finalI = i;
            historyTable.table(Tex.whiteui, t -> {
                advanceMsg thisMsg = msgList.get(finalI);
                t.background(Tex.whitePane);
                t.setColor(thisMsg.msgType.color);
                t.touchable = Touchable.enabled;
                t.marginTop(5);

                t.table(Tex.whiteui, tt -> {
                    tt.color.set(thisMsg.msgType.color);

                    tt.add(thisMsg.msgType.name).style(Styles.outlineLabel).color(thisMsg.msgType.color).left().width(200f);

                    tt.add(formatTime(thisMsg.time)).style(Styles.outlineLabel).color(thisMsg.msgType.color).left().padLeft(20f).width(100f);

                    tt.add().growX();
                    tt.add("    " + (msgList.size - finalI) + "").style(Styles.outlineLabel).color(thisMsg.msgType.color).padRight(10);

                    tt.button(Icon.copy, Styles.logici, () ->{Core.app.setClipboardText(thisMsg.message);ui.announce("已导出本条聊天记录");}).size(24f).padRight(6);
                    tt.button(Icon.cancel, Styles.logici, () -> {
                        msgList.remove(finalI);
                        build();
                    }).size(24f);

                }).growX().height(30);

                t.row();

                t.table(tt -> {
                    tt.left();
                    tt.marginLeft(4);
                    tt.setColor(thisMsg.msgType.color);
                    if(fieldMode) tt.field(thisMsg.message,nodeArea,text->{}).growX();
                    else tt.add(thisMsg.message).growX();
                }).pad(4).padTop(2).growX().grow();

                t.marginBottom(7);
            }).growX().padBottom(15f).row();
        }
    }

    public String formatTime(Date time){
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
        return sdf.format(time);
    }

    public boolean resolveMsg(String message){

        if (Marker.resolveMessage(message)) return true;
        if (resolveServerMsg(message)) return true;

        addMsg(new MessageDialog.advanceMsg(MessageDialog.arcMsgType.normal,message));

        return true;
    }

    public boolean resolveServerMsg(String message){
        Seq<String> serverMsg = Seq.with("加入了服务器","离开了服务器","自动存档完成","登录成功","经验+","[YELLOW]本局游戏时长:","[YELLOW]单人快速投票");
        for (int i=0;i<serverMsg.size;i++){
            if (message.contains(serverMsg.get(i))) {addMsg(new MessageDialog.advanceMsg(arcMsgType.serverMsg,message));return true;}
        }

        if (message.contains("小贴士")) {addMsg(new MessageDialog.advanceMsg(arcMsgType.serverTips,message));return true;}
        if (message.contains("[acid][公屏][white]")) {addMsg(new MessageDialog.advanceMsg(arcMsgType.serverToast,message));return true;}

        return false;
    }


    public void addMsg(advanceMsg msg){
        msgList.add(msg);
    }

    private void clearMsg(){
        msgList.clear();
        msgInit = 0;
        msgFinal = 0;
    }

    void exportMsg(){
        StringBuilder messageHis = new StringBuilder();
        messageHis.append("下面是[ARC").append(arcVersion).append("] 导出的游戏内聊天记录").append("\n");
        messageHis.append("*** 当前地图名称: ").append(state.map.name()).append("（模式：").append(state.rules.modeName).append("）\n");
        messageHis.append("*** 当前波次: ").append(state.wave).append("\n");

        StringBuilder messageLs = new StringBuilder();
        int messageCount = 0;
        for (int i = msgInit; i <msgFinal; i++) {
            String msg = msgList.get(i).message;
            messageLs.insert(0,msg + "\n");
            messageCount +=1;
        }

        messageHis.append("成功选取共 ").append(messageCount).append(" 条记录，如下：\n");
        messageHis.append(messageLs);
        Core.app.setClipboardText(Strings.stripGlyphs(Strings.stripColors(messageHis.toString())));
    }

    public static class advanceMsg{
        public arcMsgType msgType;
        public String message;
        public Date time;
        public String sender;

        public advanceMsg(arcMsgType msgType, String message, Date time, String sender){
            this.msgType = msgType;
            this.message = message;
            this.time = time;
            this.sender = sender;
        }

        public advanceMsg(arcMsgType msgType, String message){
            this(msgType,message,new Date());
        }

        public advanceMsg(arcMsgType msgType, String message, Date time){
            this(msgType,message,time,"null");
        }

        public String getFullInfo(){
            return message;
        }

        public advanceMsg sendMessage(){
            ui.chatfrag.addMessage(msgType.arcMsgPreFix() + message,false);
            return this;
        }
    }

    public static class arcMsgType{
        public static arcMsgType
        normal = new arcMsgType("消息",Color.gray),

        markLoc = new arcMsgType("标记","坐标",Color.valueOf("#7FFFD4")),
        markWave = new arcMsgType("标记","波次",Color.valueOf("#7FFFD4")),

        serverTips = new arcMsgType("服务器","小贴士",Color.valueOf("#98FB98")),
        serverMsg =  new arcMsgType("服务器","信息",Color.valueOf("#00FA9A")),
        serverToast =  new arcMsgType("服务器","通报",Color.valueOf("#00FA9A")),

        logicNotify = new arcMsgType("逻辑","通报",Color.violet),
        logicAnnounce = new arcMsgType("逻辑","公告",Color.valueOf("#DA70D6")),

        eventWorldLoad =  new arcMsgType("事件","载入地图",Color.valueOf("#D2691E")),
        eventCoreDestory = new arcMsgType("事件","核心摧毁",Color.valueOf("#B22222")),
        eventWave =  new arcMsgType("事件","波次",Color.valueOf("#D2691E"))
        ;

        public String name;
        public String type;
        public String subClass;
        public Color color = Color.gray;

        arcMsgType(String type,String subClass,Color color){
            this.name = subClass == "" ? type : (type + "~" + subClass);
            this.type = type;
            this.subClass = subClass;
            this.color = color;
        }

        arcMsgType(String type,Color color){
            this(type,"",color);
        }

        arcMsgType(String type){
            this(type,Color.gray);
        }

        arcMsgType(String type,String subClass){
            this(type,subClass,Color.gray);
        }

        public String arcMsgPreFix(){
            return "[#" + color.toString() + "]" + "[" + name + "][]";
        }

    }
}