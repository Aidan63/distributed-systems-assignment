package uk.aidanlee.dsp_assignment.structural.ec;

public class Component {
    /**
     * The name of this component.
     */
    public String name;

    /**
     * The entity this component is attached to.
     */
    public Entity entity;

    /**
     * Create a new empty component.
     * @param _name Name of the component.
     */
    public Component(String _name) {
        name = _name;
    }

    public boolean has(String _name) {
        return entity.has(_name);
    }
    public Component get(String _name) {
        return entity.get(_name);
    }
    public void remove(String _name) {
        entity.remove(_name);
    }
    public void add(Component _component) {
        entity.add(_component);
    }

    /**
     * Update method.
     * Called whenever the entity is updated.
     * @param _dt Delta time
     */
    public void update(float _dt) {
        //
    }

    /**
     * Called once this component has been added to an entity.
     */
    public void onadded() {
        //
    }

    /**
     * Called when this component is about to be removed from its entity.
     */
    public void onremoved() {
        //
    }

    /**
     * Destroy method.
     * Called when the entity is destroyed.
     */
    public void destroy() {
        //
    }
}
