package mindustry.squirrelModule.modules;

import arc.scene.Element;

public class Config {
    public Element[] element;
    public HackFunc func;
    public boolean enabled;
    public Config(Element[] element, HackFunc func) {
        this.element = element;
        this.func = func;
        func.config = this;
    }
}
