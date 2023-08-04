package mindustry.squirrelModule.modules;

import arc.scene.Element;

public class Config {
    public String displayName;
    public Element[] element;
    public HackFunc func;
    public boolean enabled;
    public Config(String displayName, Element[] element, HackFunc func) {
        this.displayName = displayName;
        this.element = element;
        this.func = func;
        func.config = this;
    }
}
