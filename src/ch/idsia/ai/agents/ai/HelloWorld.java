package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class HelloWorld extends BasicAIAgent implements Agent
{

    public HelloWorld(String s)
    {
        super("HelloWorld");
        reset();
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];// Empty action
    }

    public boolean[] getAction(Environment observation)
    {
        return new boolean[Environment.numberOfButtons]; // Empty action
    }

}
