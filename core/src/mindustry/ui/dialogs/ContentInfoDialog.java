package mindustry.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ContentInfoDialog extends BaseDialog{

    public ContentInfoDialog(){
        super("@info.title");

        addCloseButton();
    }

    public void show(UnlockableContent content){
        cont.clear();

        Table table = new Table();
        table.margin(10);

        //initialize stats if they haven't been yet
        content.checkStats();

        table.table(title1 -> {
            title1.image(content.uiIcon).size(iconXLarge).scaling(Scaling.fit).get().clicked(() -> Core.app.setClipboardText(content.emoji()));
            title1.add("[accent]" + content.localizedName + "\n[gray]" + content.name + (constants.lookupLogicId(content) != -1 ? " <#" + constants.lookupLogicId(content) +">": "")).padLeft(5);
        });

        table.row();

        if(content.description != null){
            var any = content.stats.toMap().size > 0;

            if(any){
                table.add("@category.purpose").color(getThemeColor()).fillX().padTop(10);
                table.row();
            }

            table.add("[lightgray]" + content.displayDescription()).wrap().fillX().padLeft(any ? 10 : 0).width(500f).padTop(any ? 0 : 10).left();
            table.row();

            if(!content.stats.useCategories && any){
                table.add("@category.general").fillX().color(getThemeColor());
                table.row();
            }
        }

        Stats stats = content.stats;

        for(StatCat cat : stats.toMap().keys()){
            OrderedMap<Stat, Seq<StatValue>> map = stats.toMap().get(cat);

            if(map.size == 0) continue;

            if(stats.useCategories){
                table.add("@category." + cat.name).color(getThemeColor()).fillX();
                table.row();
            }

            for(Stat stat : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[lightgray]" + stat.localized() + ":[] ").left().top();
                    Seq<StatValue> arr = map.get(stat);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }

                }).fillX().padLeft(10);
                table.row();
            }
        }

        if(content.details != null){
            table.add("[gray]" + (content.unlocked() || !content.hideDetails ? content.details : Iconc.lock + " " + Core.bundle.get("unlock.incampaign"))).pad(6).padTop(20).width(400f).wrap().fillX();
            table.row();
        }

        content.displayExtra(table);

        table.table(t -> {
            t.button(Icon.copy, Styles.clearNonei, () -> {
                Core.app.setClipboardText((char) Fonts.getUnicode(content.name) + "");
            });
            t.button(Icon.info, Styles.clearNonei, () -> {
                Core.app.setClipboardText(content.name + "");
            });
            t.button(Icon.book, Styles.clearNonei, () -> {
                Core.app.setClipboardText(content.description + "");
            });
            t.row();
            t.add("分享|标记：[简]");
            t.button(Icon.link,  Styles.clearNonei, () -> {
                String message = arcItemInfo(content,false);
                int seperator = 145;
                for (int i=0; i < message.length()/(float)seperator;i++){
                    Call.sendChatMessage(message.substring(i*seperator,Math.min(message.length(),(i+1)*seperator)));
                }
            }).size(30).disabled(!Core.settings.getBool("arcShareWaveInfo"));
            t.add("   ;[详]");
            t.button(Icon.link,  Styles.clearNonei, () -> {
                String message = arcItemInfo(content,true);
                int seperator = 145;
                for (int i=0; i < message.length()/(float)seperator;i++){
                    Call.sendChatMessage(message.substring(i*seperator,Math.min(message.length(),(i+1)*seperator)));
                }
            }).size(30).disabled(!Core.settings.getBool("arcShareWaveInfo"));
        }).fillX().padLeft(10);

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }

    private String arcItemInfo(UnlockableContent content,boolean description){
        String builder = "[ARC"+arcVersion+"]";
        builder+="标记了"+content.localizedName+content.emoji();
        builder+="("+content.name+")";
        if(content.description != null && description){
            builder+="。介绍: "+content.description;
        }
        return builder;
    }

}
