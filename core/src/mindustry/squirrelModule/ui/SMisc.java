package mindustry.squirrelModule.ui;

import arc.graphics.Color;
import arc.math.Mathf;

public class SMisc {
    static String color(String str, float step, float rot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, l = str.length(); i < l; i++) {
            rot = (rot + step) % 180;
            sb.append("[#").append(new Color(Color.packRgba((int) (Mathf.sin(0.1f * rot) * 127 + 128), (int) (Mathf.sin(0.1f * rot + 2 * Mathf.PI / 3) * 127 + 128), (int) (Math.sin(0.1f * rot + 4 * Mathf.PI / 3) * 127 + 128), 255)).toString(), 0, 6).append("]").append(str.charAt(i));
        }
        return sb + "[white]";
    }
}
