package ch.idsia.ai.agents.ai;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.FeatureExtractor;
import ch.idsia.tools.EvaluationInfo;
import java.util.Random;

public class HelloWorld extends BasicAIAgent implements Agent
{
    private static final int A_ACTION = 0;
    private static final int LEFT_ACTION = 1;
    private static final int RIGHT_ACTION = 2;
    private static final int A_LEFT_ACTION = 3;
    private static final int A_RIGHT_ACTION = 4;
    private static final int NOTHING_ACTION = 5;

    private static final int NUM_ACTIONS = 5;
    private static final int NUM_FEATURES = 2;
    private static final float DISCOUNT = 1;
    private static final float STEP_SIZE = 1;

    private static final float MODE_WEIGHT = 1000;
    private static final float PROGRESS_WEIGHT = 10;

    private Environment prevObv;
    private int prevAction;
    private float prevXPos = 0;
    private int prevMarioMode = 2;


    private float[][] weights = new float[NUM_ACTIONS][NUM_FEATURES];

    public HelloWorld(String s)
    {

        super("HelloWorld");
        for(int i = 0; i < NUM_ACTIONS; i++){
            for(int j = 0; j < NUM_FEATURES; j++){
                weights[i][j] = 0;
            }
        }
        
    }

    public void reset()
    {
        System.out.println("WERE RESETTING AAAAAHHHH");
        action = new boolean[Environment.numberOfButtons];// Empty action
    }

    private void print2dArray(float[][] arr){
        int len = arr.length;
        for(int i = 0; i < len; i++){
            int width = arr[i].length;
            String a = "";
            for(int j = 0; j < width; j++){
                a += arr[i][j];
            }
            System.out.println(a);
        }
    }

    private void print1dArray(float[] arr){
        int len = arr.length;
        String a = "";
        for(int i = 0; i < len; i ++){
            a += arr[i] + "  ";
        }
        System.out.println(a);
    }

    private void setAction(int actionNum){
        for(int i = 0; i < Environment.numberOfButtons; i++){
            action[i] = false;
        }
        switch(actionNum){
            case A_ACTION:
                action[Mario.KEY_JUMP] = true;
                break;
            case LEFT_ACTION:
                action[Mario.KEY_LEFT] = true;
                break;
            case RIGHT_ACTION:
                action[Mario.KEY_RIGHT] = true;
                break;
            case A_RIGHT_ACTION:
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_RIGHT] = true;
                break;
            case A_LEFT_ACTION:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
        }
    }

    private float calcQ(float[][] features, float[][] weights, int action){
        float curQ = 0;
        for(int i = 0; i < NUM_FEATURES; i++){
            curQ += features[action][i] * weights[action][i];
        }
        return curQ;
    }

    public float reward(Environment observation){
        /*if(Mario.getStatus() == Mario.STATUS_DEAD){

        }*/
        float marioModeScore = (observation.getMarioMode() - prevMarioMode) * MODE_WEIGHT;
        float marioProgressScore = (observation.getMarioFloatPos()[0] - prevXPos) * PROGRESS_WEIGHT;
        prevMarioMode = observation.getMarioMode();
        prevXPos = observation.getMarioFloatPos()[0];
        return marioModeScore + marioProgressScore;
    }

    private void incremementWeights(float error, int action){
        float[][] features = FeatureExtractor.extractFeatures(prevObv, action);
        for(int i = 0; i < NUM_FEATURES; i++){
            weights[action][i] -= features[action][i] * error * STEP_SIZE;
        }
    }


    public boolean[] getAction(Environment observation)
    {
        if(prevObv == null){
            prevObv = observation;
            Random randGen = new Random();
            int randAction = randGen.nextInt(NUM_ACTIONS);
            System.out.println("RANDOM ACITION:" + randAction);
            prevAction = randAction;
            setAction(randAction);
            return action;
        }

        //prevObv = observation;
        float[][] features = FeatureExtractor.extractFeatures(prevObv, prevAction);
        System.out.println("FEATURESSSSS");
        print2dArray(features); 
        float curQ = calcQ(features, weights, prevAction);
        float maxActionVal = -10000000;
        int maxAction = 0;
        for(int i = 0; i < NUM_ACTIONS; i++){
            float[][] curFeatures = FeatureExtractor.extractFeatures(observation, i);
            float qValue = calcQ(curFeatures, weights, i);
            if(qValue > maxActionVal) {
                maxActionVal = qValue;
                maxAction = i;
            }
        }
        float error = curQ - (reward(observation) + DISCOUNT * maxActionVal);
        incremementWeights(error, prevAction);
        prevAction = maxAction;
        Random randGen = new Random();
        int pickRand = randGen.nextInt(2);
        int chosenAction = maxAction;
        if(pickRand == 1){
            chosenAction = randGen.nextInt(NUM_ACTIONS);
            System.out.println("RANDOM: " + chosenAction);
        } else{
            System.out.println("NOT RANDOM");
        }
        setAction(chosenAction);
        System.out.println("WEIIIIGGGGHTS");
        print2dArray(weights);
        prevObv = observation;
        System.out.println("ACITION:" + chosenAction);
        return action;


        /*System.out.println(EvaluationInfo.MarioStatus);
        action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();
        action[Mario.KEY_RIGHT] = true;*/
        
    }

}





        /*byte[][] enemies = observation.getEnemiesObservation();
        byte[][] completeObv = observation.getCompleteObservation();
        byte[][] levelObv = observation.getLevelSceneObservation();
        System.out.println("enemies");
        print2dArray(enemies);
        System.out.println("complete");
        print2dArray(completeObv);
        System.out.println("levelObv");
        print2dArray(levelObv);
        float[] mposition = observation.getMarioFloatPos();
        float[] eposition = observation.getEnemiesFloatPos();
        System.out.println("mario position");
        print1dArray(mposition);
        System.out.println("enemies position");
        print1dArray(eposition);
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();*/
