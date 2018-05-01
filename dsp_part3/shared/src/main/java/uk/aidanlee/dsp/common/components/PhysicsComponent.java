package uk.aidanlee.dsp.common.components;

import uk.aidanlee.dsp.common.structural.ec.Component;
import uk.aidanlee.dsp.common.utils.MathsUtil;

/**
 * Physics component.
 * It simulates the ship when updated according to the input keys and stats.
 */
public class PhysicsComponent extends Component {
    public PhysicsComponent(String _name) {
        super(_name);
    }

    @Override
    public void update(float _dt) {
        if (has("stats") && has("input") && has("velocity")) {

            // Get all of the other attached components needed for the simulation.
            StatsComponent stats = (StatsComponent) get("stats");
            InputComponent input = (InputComponent) get("input");
            VelocityComponent velocity = (VelocityComponent) get("velocity");

            // ACCELERATION
            if (input.accelerate)
            {
                stats.engineSpeed += (stats.maxSpeed - stats.engineSpeed) / (100 - stats.accSpeed);
            }
            else if (input.decelerate)
            {
                stats.engineSpeed -= ((stats.maxSpeed / 2) - Math.abs(Math.min(0, stats.engineSpeed))) / (100 - stats.accSpeed);
            }
            else
            {
                stats.engineSpeed *= stats.speedDecay;
            }

            stats.acceleration += (stats.engineSpeed - stats.acceleration);

            // STEERING

            // Normal steering.
            if (input.steerLeft && stats.wheelDir >- stats.turnSpeedLimit)
            {
                stats.wheelDir -= stats.turnSpeed;
            }
            if (input.steerRight && stats.wheelDir < stats.turnSpeedLimit)
            {
                stats.wheelDir += stats.turnSpeed;
            }

            // Air brakes can be used if we are moving.
            if (stats.engineSpeed > 2)
            {
                if (input.airBrakeLeft && stats.wheelDir > -stats.turnSpeedLimit)
                {
                    stats.wheelDir -= stats.abTurnSpeed;
                    stats.engineSpeed -= 0.1;
                }
                if (input.airBrakeRight && stats.wheelDir < stats.turnSpeedLimit)
                {
                    stats.wheelDir += stats.abTurnSpeed;
                    stats.engineSpeed -= 0.1;
                }
            }

            // If we are not steering slow the turn rate to 0.
            if (!input.steerLeft && !input.steerRight && !input.airBrakeLeft && !input.airBrakeRight)
            {
                stats.wheelDir *= stats.rotationDecay;
            }

            // Apply this steering change to the direction and ship angle.
            entity.rotation += stats.wheelDir * stats.maxSpeed / stats.turnMod;
            stats.direction += ((Math.sin(Math.toRadians(entity.rotation - stats.direction)) * stats.maxSpeed) / Math.max(1, stats.acceleration) * stats.grip / 10);

            // DRIFT
            stats.skid = (float)Math.abs(Math.sin(Math.toRadians(entity.rotation - stats.direction)));
            stats.acceleration -= (stats.skid * stats.grip) / 200;

            // Limit and apply acceleration
            stats.acceleration = Math.min(stats.acceleration, stats.topSpeed);
            velocity.x += MathsUtil.lengthdirX(stats.acceleration, stats.direction);
            velocity.y += MathsUtil.lengthdirY(stats.acceleration, stats.direction);
        }
    }
}
