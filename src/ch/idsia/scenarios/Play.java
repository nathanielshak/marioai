package ch.idsia.scenarios;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.mario.engine.sprites.Mario;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */
public class Play {

    public static void main(String[] args) {
        boolean observe = true;
        Agent controller = new HumanKeyboardAgent();
        boolean maxSpeed = false;
        int difficulty = 0;
        if (args.length > 0) {
            controller = AgentsPool.load (args[0]);
            AgentsPool.addAgent(controller);
        }
        int numTrials = 1;
        if (args.length > 1){
            numTrials = Integer.parseInt(args[1]);
            maxSpeed = true;
        }    
        if (args.length > 2){ //enter any third argument to set difficulty
            difficulty = Integer.parseInt(args[2]);
        }
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(controller);
        Task task = new ProgressTask(options);
        options.setMaxFPS(maxSpeed);
        options.setVisualization(observe);
        options.setNumberOfTrials(numTrials);
        options.setMatlabFileName("");
        options.setLevelDifficulty(difficulty);
        task.setOptions(options);
        for(int i = 0; i < numTrials; i++){
            options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));

            System.out.println (task.evaluate (controller)[0] + ",");
            //System.out.println(task.evalInfo().marioStatus);
            
            controller.signalStatus(task.evalInfo().marioStatus);
        }
        
    }
}
