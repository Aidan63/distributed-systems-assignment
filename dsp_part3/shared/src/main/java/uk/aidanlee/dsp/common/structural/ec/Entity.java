package uk.aidanlee.dsp.common.structural.ec;

import com.badlogic.gdx.math.Vector2;
import com.google.common.eventbus.EventBus;
import uk.aidanlee.dsp.common.structural.ModList;

import java.util.*;

/**
 * Entity class. Entities contain some basic spatial information (pos, rotation, origin) acts as a holder for components.
 * Components are freely capable of modifying the entity they are attached to and accessing other components.
 */
public class Entity {
    /**
     * Unique ID of this entity.
     */
    private String id;

    /**
     * Name of this entity.
     */
    private String name;

    /**
     * List of all components attached to this entity.
     * Mod list is used since component will want to add and remove other component while the components are being iterated over.
     */
    private ModList<Component> components;

    /**
     * Events for this entity.
     */
    private final EventBus events;

    /**
     * Position of this entity.
     */
    public Vector2 pos;

    /**
     * Origin point of this entity.
     */
    public Vector2 origin;

    /**
     * Rotation of this entity.
     */
    public float rotation;

    /**
     * Creates a new entity.
     * @param _name Name of the entity.
     */
    public Entity(String _name) {
        name = _name;
        id   = UUID.randomUUID().toString();

        pos      = new Vector2(0, 0);
        origin   = new Vector2(0, 0);
        rotation = 0;

        components = new ModList<>();
        events     = new EventBus();
    }

    // Getters and Setters

    public ModList<Component> getComponents() {
        return components;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public EventBus getEvents() {
        return events;
    }

    // Public API

    /**
     * Adds the provided component to the entity.
     * @param _component The component to add.
     * @return The component added.
     */
    public Component add(Component _component) {
        components.add(_component);
        _component.entity = this;

        _component.onadded();

        return _component;
    }

    /**
     * Removes the component attached to this entity with the provided name.
     * @param _name The name of the component to add.
     */
    public void remove(String _name) {
        for (Component comp : components) {

            if (comp.name.equals(_name)) {
                comp.onremoved();
                components.remove(comp);

                return;
            }
        }
    }

    /**
     * Checks if the entity has a component with the provided name attached.
     * @param _name The component name to check.
     * @return If the component has the component attached.
     */
    public boolean has(String _name) {
        for (Component comp : components) {

            if (comp.name.equals(_name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the component attached to the entity which has the provided name.
     * @param _name The name of the component.
     * @return The component found.
     */
    public Component get(String _name) {
        for (Component comp : components) {

            if (comp.name.equals(_name)) {
                return comp;
            }
        }

        return null;
    }

    /**
     * Updates the entity and all its components.
     * @param _dt Delta time.
     */
    public void update(float _dt) {
        for (Component comp : components) {
            comp.update(_dt);
        }
    }

    /**
     * Removes the entity and all of its components.
     */
    public void destroy() {
        for (Component comp : components) {
            comp.destroy();
        }
    }
}
