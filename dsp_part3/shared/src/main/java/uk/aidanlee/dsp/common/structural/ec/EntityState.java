package uk.aidanlee.dsp.common.structural.ec;

import uk.aidanlee.dsp.common.structural.ModMap;

public class EntityState {
    private ModMap<String, Component> components;

    EntityState() {
        components = new ModMap<>();
    }

    public EntityState add(Component _component) {
        components.set(_component.name, _component);
        return this;
    }

    public ModMap<String, Component> getComponents() {
        return components;
    }
}
