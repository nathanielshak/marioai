package ch.idsia.ai.agents.ai;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class HelloWorld extends BasicAIAgent implements Agent
{

    public HelloWorld(String s)
    {
        super("HelloWorld");
        
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];// Empty action
    }

    public boolean[] getAction(Environment observation)
    {
        byte[][] wee = observation.getEnemiesObservation();
        int len = wee.length;
        for(int i = 0; i < len; i++){
            int width = wee[i].length;
            String a = "";
            for(int j = 0; j < width; j++){
                a += wee[i][j];
            }
            System.out.println(a);
        }
        action[Mario.KEY_RIGHT] = true;
        return action;
    }

}
