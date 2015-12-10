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

    //private static final int NUM_ACTIONS = 5;
    //private static final int NUM_FEATURES = 12;
    private static final double DISCOUNT = 1;
    private static final double STEP_SIZE = 0.2;

    private static final double MODE_WEIGHT = 0;
    private static final double PROGRESS_WEIGHT = 5;
    private static final double Y_PROGRESS_WEIGHT = 1;

    private Environment prevObv;
    private int prevAction;
    private double prevXPos = 0;
    private int prevMarioMode = 2;
    private double prevYPos = 0;
    private int randomRange = 5;
    private int trial = 0;

    private Random randGen = new Random();



    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];

    public HelloWorld(String s)
    {

        super("HelloWorld");
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++){
            for(int j = 0; j < FeatureExtractor.NUM_FEATURES; j++){
                weights[i][j] = 0;
            }
        }
        System.out.println("MEOWWW Weights: ");
        print2dArray(weights);
        
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];// Empty action
        System.out.println("RESET Weights: ");
        print2dArray(weights);
        //randomRange ++;
    }

    private void print2dArray(double[][] arr){
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

    private void print1dArray(double[] arr){
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

    private double calcQ(double[][] features, double[][] weights, int action){
        double curQ = 0;
        for(int i = 0; i < FeatureExtractor.NUM_FEATURES; i++){
            curQ += features[action][i] * weights[action][i];
        }
        return curQ;
    }

    public double reward(Environment observation){
        /*if(Mario.getStatus() == Mario.STATUS_DEAD){

        }*/
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
    private void printWeights(double[][] vec){
        //System.out.println("WEIGHTS");
        for(int i = 0; i< vec.length; i++)
        {
            double sum = 0;
            for(int j = 0; j < vec.length; j++)
            {
                sum += vec[i][j];
            }
            //System.out.println("action = " + i + " weight = " + sum);
        }
    }

    private void incremementWeights(double error, int action){
        double[][] features = FeatureExtractor.extractFeatures(prevObv, action);
        for(int i = 0; i < FeatureExtractor.NUM_FEATURES; i++){
            weights[action][i] -= features[action][i] * error * STEP_SIZE;
        }
    }

    private boolean uselessAction(int action, Environment observation){
        if((action == A_LEFT_ACTION || action == A_RIGHT_ACTION || action == A_ACTION) && !observation.mayMarioJump()){
            return true;
        }
        return false;   
    }


    public boolean[] getAction(Environment observation)
    {
        if(prevObv == null)
        { //first action
            /*prevObv = observation;
            Random randGen = new Random();
            int randAction = randGen.nextInt(NUM_ACTIONS);
            //System.out.println("RANDOM ACITION:" + randAction);
            prevAction = randAction;
            setAction(randAction);*/
            prevObv = observation;
            prevAction = A_RIGHT_ACTION;
            setAction(A_RIGHT_ACTION);
            return action;
        }


        //prevObv = observation;
        double[][] features = FeatureExtractor.extractFeatures(prevObv, prevAction);
        double curQ = calcQ(features, weights, prevAction);
        double maxActionVal = -10000000;
        int maxAction = 0;
        double reward = 0;
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++)
        {
            //System.out.println("Mario can jump? " + observation.mayMarioJump());
            //System.out.println("checking action: " + i);
            if(uselessAction(i, observation)){
                //System.out.println("WE GOT A USELESS ACTION: " + i);
                continue;
            }
            double[][] curFeatures = FeatureExtractor.extractFeatures(observation, i);
            double qValue = calcQ(curFeatures, weights, i);
            //System.out.println("Q = " + qValue);
            if(qValue >= maxActionVal) {
                maxActionVal = qValue;
                maxAction = i;
                reward = maxActionVal;
            }
        }
        double error = curQ - (reward(observation) + DISCOUNT * maxActionVal);
        incremementWeights(error, prevAction);
        prevAction = maxAction;
        int pickRand = randGen.nextInt(randomRange);
        int chosenAction = maxAction;
        if(pickRand == 1)
        {
            //System.out.println("RAND ACTION: " + chosenAction);
            chosenAction = randGen.nextInt(FeatureExtractor.NUM_ACTIONS);
            //System.out.println("RANDOM: " + chosenAction);
        } else{
            //System.out.println("MAX ACTION: " + chosenAction);
        }
        //System.out.println("REWARD: " + reward);
        printWeights(weights);
        setAction(chosenAction);
        prevObv = observation;

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
        double[] mposition = observation.getMarioFloatPos();
        double[] eposition = observation.getEnemiesdoublePos();
        System.out.println("mario position");
        print1dArray(mposition);
        System.out.println("enemies position");
        print1dArray(eposition);
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();*/
