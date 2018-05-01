package uk.aidanlee.dsp.common.structural.ec;

import uk.aidanlee.dsp.common.structural.ModMap;

/**
 * State machine for entities and components. A state in ECS is explicitly defined by the components attached to the entity.
 * The state machine holds a list of possible entity states. These state contain all of the components which should be attached when the entity is in that state.
 * When a entities state is set all of the components in the previous state are removed and all components in the new state are added.
 * The entity state machine is itself a component and should be attached to the entity it will modify.
 */
public class EntityStateMachine extends Component {

    /**
     * Map of all the entity states. Keyed by a string name.
     */
    private ModMap<String, EntityState> states;

    /**
     * The current entity state.
     */
    private EntityState currentState;

    /**
     * current entity states name.
     */
    private String state;

    /**
     * Creates a new entity state machine component.
     * @param _name Component name.
     */
    public EntityStateMachine(String _name) {
        super(_name);
        states = new ModMap<>();
    }

    /**
     * Gets the current state name.
     * @return String.
     */
    public String getState() {
        return state;
    }

    /**
     * Create a new entity state and return it to allow adding components.
     * @param _name Name of the state.
     * @return Created EntityState instance.
     */
    public EntityState createState(String _name) {
        EntityState state = new EntityState();
        states.set(_name, state);

        return state;
    }

    /**
     * Changes the state of this state machine.
     * @param _name State to switch to.
     */
    public void changeState(String _name) {

        // Ensure we have the state we want to change to and its not the current state.
        if (!states.exists(_name)) return;

        EntityState newState = states.get(_name);
        if (newState == currentState) return;

        // Remove all existing states components.
        if (currentState != null) {
            for (String key : currentState.getComponents()) {
                remove(key);
            }
        }

        // Add new components for this state.
        for (String key : newState.getComponents()) {
            add(newState.getComponents().get(key));
        }

        currentState = newState;
        state = _name;
    }
}
