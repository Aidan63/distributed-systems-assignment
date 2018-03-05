package uk.aidanlee.dsp_assignment.structural.ec;

import uk.aidanlee.dsp_assignment.structural.ModMap;

public class EntityStateMachine extends Component {
    private ModMap<String, EntityState> states;
    private EntityState currentState;

    public EntityStateMachine(String _name) {
        super(_name);
        states = new ModMap<>();
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

        if (currentState != null) {
            for (String key : states) {
                if (newState.getComponents().exists(key)) remove(key);
            }
        }

        for (String key : newState.getComponents()) {
            if (!has(key)) add(newState.getComponents().get(key));
        }

        currentState = newState;
    }
}
