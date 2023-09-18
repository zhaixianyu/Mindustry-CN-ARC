package mindustry.type;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.world.meta.*;

public class StatusEffect extends UnlockableContent{
    /** Damage dealt by the unit with the effect. */
    public float damageMultiplier = 1f;
    /** Unit health multiplier. */
    public float healthMultiplier = 1f;
    /** Unit speed multiplier. */
    public float speedMultiplier = 1f;
    /** Unit reload multiplier. */
    public float reloadMultiplier = 1f;
    /** Unit build speed multiplier. */
    public float buildSpeedMultiplier = 1f;
    /** Unit drag multiplier. */
    public float dragMultiplier = 1f;
    /** Damage dealt upon transition to an affinity. */
    public float transitionDamage = 0f;
    /** Unit weapon(s) disabled. */
    public boolean disarm = false;
    /** Damage per frame. */
    public float damage;
    /** Chance of effect appearing. */
    public float effectChance = 0.15f;
    /** Should the effect be given a parent. */
    public boolean parentizeEffect;
    /** If true, the effect never disappears. */
    public boolean permanent;
    /** If true, this effect will only react with other effects and cannot be applied. */
    public boolean reactive;
    /** Whether to show this effect in the database. */
    public boolean show = true;
    /** Tint color of effect. */
    public Color color = Color.white.cpy();
    /** Effect that happens randomly on top of the affected unit. */
    public Effect effect = Fx.none;
    /** Effect that is displayed once when applied to a unit. */
    public Effect applyEffect = Fx.none;
    /** Whether the apply effect should display even if effect is already on the unit. */
    public boolean applyExtend;
    /** Tint color of apply effect. */
    public Color applyColor = Color.white.cpy();
    /** Should the apply effect be given a parent. */
    public boolean parentizeApplyEffect;
    /** Affinity & opposite values for stat displays. */
    public ObjectMap<StatusEffect, StatValue> affinities = new ObjectMap<>(), reacts = new ObjectMap<>();
    public ObjectSet<StatusEffect> opposites = new ObjectSet<>();
    /** Set to false to disable outline generation. */
    public boolean outline = true;
    /** Transition handler map. */
    protected ObjectMap<StatusEffect, TransitionHandler> transitions = new ObjectMap<>();
    /** Called on init. */
    protected Runnable initblock = () -> {};

    public StatusEffect(String name){
        super(name);
    }

    @Override
    public void init(){
        if(initblock != null){
            initblock.run();
        }
        details = "若单位有效果A时被添加效果B会产生反应\n则称A亲和B，B反应A\n(如果触发反应，效果B不会挂到单位上)\n永久效果:此buff不会随时间消失\n瞬间效果:只能触发反应，不会挂到单位上";
    }

    public void init(Runnable run){
        this.initblock = run;
    }

    @Override
    public boolean isHidden(){
        return localizedName.equals(name) || !show;
    }

    @Override
    public void setStats(){
        stats.add("瞬间效果", StatCat.general, StatValues.bool(reactive));
        stats.add("永久效果", StatCat.general, StatValues.bool(permanent));
        if(damageMultiplier != 1) stats.addPercent(Stat.damageMultiplier, damageMultiplier);
        if(healthMultiplier != 1) stats.addPercent(Stat.healthMultiplier, healthMultiplier);
        if(speedMultiplier != 1) stats.addPercent(Stat.speedMultiplier, speedMultiplier);
        if(reloadMultiplier != 1) stats.addPercent(Stat.reloadMultiplier, reloadMultiplier);
        if(buildSpeedMultiplier != 1) stats.addPercent(Stat.buildSpeedMultiplier, buildSpeedMultiplier);
        if(dragMultiplier != 1) stats.addPercent("移动阻力倍率", StatCat.general, dragMultiplier);
        if(damage > 0) stats.add(Stat.damage, damage * 60f, StatUnit.perSecond);
        if(damage < 0) stats.add(Stat.healing, -damage * 60f, StatUnit.perSecond);

        for(var e : opposites.toSeq().sort()){
            stats.add(Stat.opposites, e.emoji() + "" + e);
        }

        for (var e : affinities) {
            stats.add(Stat.affinities, e.value);
        }
        for (var e : reacts) {
            stats.add(Stat.reactive, e.value);
        }
    }

    @Override
    public boolean showUnlock(){
        return false;
    }

    /** Runs every tick on the affected unit while time is greater than 0. */
    public void update(Unit unit, float time){
        if(damage > 0){
            unit.damageContinuousPierce(damage);
        }else if(damage < 0){ //heal unit
            unit.heal(-1f * damage * Time.delta);
        }

        if(effect != Fx.none && Mathf.chanceDelta(effectChance)){
            Tmp.v1.rnd(Mathf.range(unit.type.hitSize/2f));
            effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 0, color, parentizeEffect ? unit : null);
        }
    }

    protected void trans(StatusEffect effect, TransitionHandler handler){
        transitions.put(effect, handler);
    }
    protected void affinity(StatusEffect effect, TransitionHandler handler, String... desc){
        affinities.put(effect, StatValues.statusReact(effect, desc));
        effect.reacts.put(this, StatValues.statusReact(this, desc));
        trans(effect, handler);
    }

    protected void opposite(StatusEffect... effect){
        for(var other : effect){
            handleOpposite(other);
            other.handleOpposite(this);
        }
    }

    protected void handleOpposite(StatusEffect other){
        opposites.add(other);
        trans(other, (unit, result, time) -> {
            result.time -= time * 0.5f;
            if(result.time <= 0f){
                result.time = time;
                result.effect = other;
            }
        });
    }

    public void draw(Unit unit, float time){
        draw(unit); //Backwards compatibility
    }

    public void draw(Unit unit){

    }

    public boolean reactsWith(StatusEffect effect){
        return transitions.containsKey(effect);
    }

    /**
     * Called when transitioning between two status effects.
     * @param to The state to transition to
     * @param time The applies status effect time
     * @return whether a reaction occurred
     */
    public boolean applyTransition(Unit unit, StatusEffect to, StatusEntry entry, float time){
        var trans = transitions.get(to);
        if(trans != null){
            trans.handle(unit, entry, time);
            return true;
        }
        return false;
    }

    public void applied(Unit unit, float time, boolean extend){
        if(!extend || applyExtend) applyEffect.at(unit.x, unit.y, 0, applyColor, parentizeApplyEffect ? unit : null);
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        if(outline){
            makeOutline(PageType.ui, packer, uiIcon, true, Pal.gray, 3);
        }
    }

    @Override
    public String toString(){
        return localizedName;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.status;
    }

    public interface TransitionHandler{
        void handle(Unit unit, StatusEntry current, float time);
    }
}
