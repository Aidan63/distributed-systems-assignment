package uk.aidanlee.dsp.common.structural.ec;

import uk.aidanlee.dsp.common.structural.ModMap;

/**
 * A state an entity can be in.
 */
public class EntityState {
    /**
     * All of the components which will be attached to the entity when in this state.
     */
    private ModMap<String, Component> components;

    EntityState() {
        components = new ModMap<>();
    }

    /**
     * Add a component to this state.
     * @param _component Component to add.
     * @return This EntityState instance for chaining.
     */
    public EntityState add(Component _component) {
        components.set(_component.name, _component);
        return this;
    }

    /**
     * Returns all of the components in this state.
     * @return Map of components keyed by their string name.
     */
    public ModMap<String, Component> getComponents() {
        return components;
    }
}
