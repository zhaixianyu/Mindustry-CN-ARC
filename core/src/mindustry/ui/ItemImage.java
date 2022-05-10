package mindustry.ui;

import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.core.*;
import mindustry.type.*;
import static mindustry.Vars.*;

public class ItemImage extends Stack{

    public ItemImage(TextureRegion region, int reqAmount, int curAmount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        add(new Table(t -> {
            t.left().bottom();
            t.add(reqAmount + "").get().setFontScale(1f);
            t.pack();
        }));
        add(new Table(t -> {
            t.left().top();
            t.add(curAmount + "").get().setFontScale(0.6f);
            t.pack();
            //}).visible(() -> !player.team().rules().cheat));//无法判断工厂队伍
        }));
    }

    public ItemImage(TextureRegion region, int amount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        if(amount != 0){
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount(amount) : amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public ItemImage(ItemStack stack){
        this(stack.item.uiIcon, stack.amount);
    }

    public ItemImage(PayloadStack stack){
        this(stack.item.uiIcon, stack.amount);
    }
}
