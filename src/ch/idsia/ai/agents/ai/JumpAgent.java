package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.ai.agents.ai;
 */

public class JumpAgent extends BasicAIAgent implements Agent {

    static final boolean superslow = false;

    public JumpAgent()
    {
        super("JumpAgent");
        reset();
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = false;
        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
//        try {Thread.sleep (39);}
//        catch (Exception e){}
        //action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();
        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = true;
        return action;
    }
}
