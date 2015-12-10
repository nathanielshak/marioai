package ch.idsia.ai.agents.human;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.FeatureExtractor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class HumanKeyboardAgent extends KeyAdapter implements Agent
{
    List<boolean[]> history = new ArrayList<boolean[]>();
    private boolean[] Action = null;
    private String Name = "HumanKeyboardAgent";
    private double prevYPos = 0;

    //private static final int NUM_ACTIONS = 5;
    //private static final int NUM_FEATURES = 12;

    //the above and below constants are stored in FeatureExtractor

    /*
    private static final int ON_GROUND = 0;
    private static final int CAN_JUMP = 1;
    private static final int DANGER_OF_GAP = 2;
    private static final int MARIO_FACING_LEDGE = 3;
    private static final int SMALL_LEDGE = 4;
    private static final int MEDIUM_LEDGE = 5;
    private static final int LARGE_LEDGE = 6;
    private static final int MARIO_IS_STUCK = 7;
    private static final int MARIO_IN_FRONT_LEDGE = 8;
    private static final int MARIO_CLOSE_TO_LEDGE = 9;
    private static final int MARIO_NOT_CLOSE_TO_LEDGE = 10;
    private static final int IN_FRONT_OF_LEDGE_JUMPED = 11;
    private static final int AIR_FACING_LEDGE = 12;
    */

    private static final double MODE_WEIGHT = 0;
    private static final double PROGRESS_WEIGHT = 5;
    private static final double Y_PROGRESS_WEIGHT = 1;

    private static double[][] curFeatures;

    public HumanKeyboardAgent()
    {
        this.reset ();
//        RegisterableAgent.registerAgent(this);
    }

    public void reset()
    {
        // Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
        Action = new boolean[Environment.numberOfButtons];
    }

    public boolean[] getAction(Environment observation)
    {
        float[] enemiesPos = observation.getEnemiesFloatPos();
        boolean print = false;
        for(int i=0; i< Action.length; i++){
            if(Action[i]!=false){
                print = true;
                break;
            }
        }
        /*
        if(print){
            System.out.print("ACTION: ");
            System.out.println(Arrays.toString(Action));
        }
    */

        curFeatures = FeatureExtractor.extractFeatures(observation, 0);
        /*
        System.out.println("Features:");
        printFeatures();

        //System.out.println("REWARD: " + reward(observation));

        */
       
        return Action;
    }

/*
    public double reward(Environment observation){
        if(Mario.getStatus() == Mario.STATUS_DEAD){

        }
        double marioModeScore = (observation.getMarioMode() - prevMarioMode) * MODE_WEIGHT;
        double marioProgressScore = (observation.getMarioFloatPos()[0] - prevXPos - 1) * PROGRESS_WEIGHT;
        prevMarioMode = observation.getMarioMode();
        prevXPos = observation.getMarioFloatPos()[0];
        double marioYProgress = (prevYPos - observation.getMarioFloatPos()[1]) * Y_PROGRESS_WEIGHT;
        if(observation.isMarioOnGround())
        {
            prevYPos = observation.getMarioFloatPos()[1];
        } else{
            marioYProgress = 0;
        }
        double reward = marioProgressScore + marioYProgress;
        return marioProgressScore + marioYProgress;
    }
*/

    private void printFeatures(){
        /*
        System.out.println("ON_GROUND: " + curFeatures[0][FeatureExtractor.ON_GROUND]);
        System.out.println("CAN_JUMP: " + curFeatures[0][FeatureExtractor.CAN_JUMP]);
        System.out.println("DANGER_OF_GAP: " + curFeatures[0][FeatureExtractor.DANGER_OF_GAP]);
        System.out.println("MARIO_FACING_LEDGE: " + curFeatures[0][FeatureExtractor.MARIO_FACING_LEDGE]);
        System.out.println("SMALL_LEDGE: " + curFeatures[0][FeatureExtractor.SMALL_LEDGE]);
        System.out.println("MEDIUM_LEDGE: " + curFeatures[0][FeatureExtractor.MEDIUM_LEDGE]);
        System.out.println("LARGE_LEDGE: " + curFeatures[0][FeatureExtractor.LARGE_LEDGE]);
        System.out.println("MARIO_IS_STUCK: " + curFeatures[0][FeatureExtractor.MARIO_IS_STUCK]);
        System.out.println("MARIO_IN_FRONT_LEDGE: " + curFeatures[0][FeatureExtractor.MARIO_IN_FRONT_LEDGE]);
        System.out.println("MARIO_CLOSE_TO_LEDGE: " + curFeatures[0][FeatureExtractor.MARIO_CLOSE_TO_LEDGE]);
        System.out.println("MARIO_NOT_CLOSE_TO_LEDGE: " + curFeatures[0][FeatureExtractor.MARIO_NOT_CLOSE_TO_LEDGE]);
        System.out.println("IN_FRONT_OF_LEDGE_JUMPED: " + curFeatures[0][FeatureExtractor.IN_FRONT_OF_LEDGE_JUMPED]);
        System.out.println("AIR_FACING_LEDGE: " + curFeatures[0][FeatureExtractor.AIR_FACING_LEDGE]);
        System.out.println("ENEMY_CLOSE: " + curFeatures[0][FeatureExtractor.ENEMY_CLOSE]);
        System.out.println("ENEMY_ABOVE: " + curFeatures[0][FeatureExtractor.ENEMY_ABOVE]);
        System.out.println("ENEMY_BEHIND: " + curFeatures[0][FeatureExtractor.ENEMY_BEHIND]);
        System.out.println("ENEMY_BELOW: " + curFeatures[0][FeatureExtractor.ENEMY_BELOW]);
        System.out.println("ENEMY_FRONT: " + curFeatures[0][FeatureExtractor.ENEMY_FRONT]);
        */
        
    }

    public AGENT_TYPE getType() {        return AGENT_TYPE.HUMAN;    }

    public String getName() {   return Name; }

    public void setName(String name) {        Name = name;    }


    public void keyPressed (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
        System.out.println("sdf");
    }

    public void keyReleased (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;
            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }
    }

   public List<boolean[]> getHistory () {
       return history;
   }
}
