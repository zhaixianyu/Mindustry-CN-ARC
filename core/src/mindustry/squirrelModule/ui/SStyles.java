package mindustry.squirrelModule.ui;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.NinePatch;
import arc.scene.style.Drawable;
import arc.scene.style.NinePatchDrawable;

public class SStyles {
    static int cornerRadius = 50, size = 512;
    static int cornerColor = new Color(60, 60, 60).rgba8888();
    public static Drawable cornerDrawable;

    {
        Pixmap pixmap = new Pixmap(size, size);
        pixmap.fillRect(cornerRadius, 0, size - 2 * cornerRadius, size, cornerColor);
        pixmap.fillRect(0, cornerRadius, size, size - 2 * cornerRadius, cornerColor);
        pixmap.fillCircle(cornerRadius, cornerRadius, cornerRadius, cornerColor);
        pixmap.fillCircle(cornerRadius, size - cornerRadius, cornerRadius, cornerColor);
        pixmap.fillCircle(size - cornerRadius, cornerRadius, cornerRadius, cornerColor);
        pixmap.fillCircle(size - cornerRadius, size - cornerRadius, cornerRadius, cornerColor);
        NinePatch borderPatch = new NinePatch(new Texture(pixmap), cornerRadius, cornerRadius, cornerRadius, cornerRadius);
        pixmap.dispose();
        cornerDrawable = new NinePatchDrawable(borderPatch);
    }
}
