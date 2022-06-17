package mindustry.arcModule;

import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.Tooltip.*;
import arc.scene.ui.layout.*;

public class ElementUtils{

    public static <T extends Element> T tooltip(T element, String text){
        return tooltip(element, text, true);
    }

    public static <T extends Element> T tooltip(T element, String text, boolean allowMobile){
        Tooltip tooltip = Tooltips.getInstance().create(text);
        tooltip.allowMobile = allowMobile;

        element.addListener(tooltip);
        return element;
    }

    public static <T extends Element> T tooltip(T element, Cons<Table> builder){
        return tooltip(element, builder, true);
    }

    public static <T extends Element> T tooltip(T element, Cons<Table> builder, boolean allowMobile){
        Tooltip tooltip = new Tooltip(builder);
        tooltip.allowMobile = allowMobile;

        element.addListener(tooltip);
        return element;
    }

}
