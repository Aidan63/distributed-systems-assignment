package uk.aidanlee.dsp.common.structural.ec;

import uk.aidanlee.dsp.common.structural.ModMap;

public class EntityStateMachine extends Component {
    private ModMap<String, EntityState> states;
    private EntityState currentState;
    private String state;

    public EntityStateMachine(String _name) {
        super(_name);
        states = new ModMap<>();
    }

    public String getState() {
        return state;
    }

    public EntityState createState(String _name) {
        EntityState state = new EntityState();
        states.set(_name, state);

        return state;
    }

    public void changeState(String _name) {
        if (!states.exists(_name)) return;

        EntityState newState = states.get(_name);
        if (newState == currentState) return;

        // Remove all existing components.
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
