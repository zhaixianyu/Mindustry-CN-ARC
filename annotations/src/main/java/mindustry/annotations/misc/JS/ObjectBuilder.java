package mindustry.annotations.misc.JS;

import java.util.HashMap;

public class ObjectBuilder extends JSBuilder{
    HashMap<String, JSBuilder> map = new HashMap<>();
    int spaces;
    ObjectBuilder(int spaces) {
        this.spaces = spaces;
        noWarp = true;
        noSemicolon = true;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder("{");
        int codeSpaces = spaces + 4;
        for(String name : map.keySet()) {
            JSBuilder code = map.get(name);
            if(code.isBlock) continue;
            sb.append("\n").append(calcSpace(codeSpaces)).append(name).append(" : ").append(code.build()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n").append(calcSpace(spaces)).append("}");
        return sb.toString();
    }
    public ObjectBuilder set(String name, JSBuilder code) {
        map.put(name, code);
        return this;
    }
    public JSBuilder get(String name) {
        return map.get(name);
    }
    public ObjectBuilder remove(String name) {
        map.remove(name);
        return this;
    }
}
