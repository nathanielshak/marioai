package ch.idsia.ai.agents.ai;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.FeatureExtractor;
import ch.idsia.tools.EvaluationInfo;
import java.util.Random;
import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.MarioComponent;
import java.util.Arrays;

public class HelloWorld extends BasicAIAgent implements Agent
{
    private static final int A_ACTION = 0;
    private static final int LEFT_ACTION = 1;
    private static final int RIGHT_ACTION = 2;
    private static final int A_LEFT_ACTION = 3;
    private static final int A_RIGHT_ACTION = 4;
    private static final int NOTHING_ACTION = 5;
    private static final int NUM_FRAMES = 5;

    //private static final int NUM_ACTIONS = 5;
    //private static final int NUM_FEATURES = 12;
    private static final double DISCOUNT = 1;
    private static final double STEP_SIZE = 0.1;

    private static final double MODE_WEIGHT = 5;
    private static final double PROGRESS_WEIGHT = 5;
    private static final double Y_PROGRESS_WEIGHT = 15;
    private static final double MARIO_STUCK_WEIGHT = 0;

    private Environment prevObv;
    private int prevAction;
    private double prevXPos = 0;
    private int prevMarioMode = 2;
    private double prevYPos = 0;
    private int randomRange = 5;
    private int trial = 0;
    private boolean firstTrial = true;
    private int frames = NUM_FRAMES;
    private double pastXPos = 0;
    private Random randGen = new Random();
    private int jumpCounter = 0;
    private boolean highJumping = false;



    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];
    private double[][] testWeights = new double[][]{{25874.34271232467, 2328.232787177108, 0.0, -1848.4816909653614, -368.1822698115061, -368.7720704177998, -3523.75220473707, 0.0, -9597.138455124457, 2556.464670408757, 5192.192093750352, 1684.6746794985002, -2029.116607528068, 0.0, 669.7684927065004, 699.3174568061415, 777.7973089714915, 325.39938312515505}, {25975.212583968474, 20217.060374415494, 3.002030772462195, -1960.954584121408, -350.0210319644584, -253.77098845024793, -3535.8068055870613, 0.0, -9636.499893399168, 2538.8152361968264, 5136.730073080906, 1684.652076201561, -1906.3699511291484, 0.0, 600.8464276920881, 744.5763033210309, 768.3834140239599, 346.35992602576613}, {26095.90189614773, 20211.581364751986, 1.7469733720314256, -1995.8084835226398, -342.35994271176634, -248.91492525171765, -3494.1681904658776, 0.0, -9665.46029620991, 2532.396273907964, 5137.255538779271, 1710.4057905149036, -1846.8191669006676, 0.0, 621.504150399456, 707.1323464297355, 840.3335127521347, 344.5006629860024}, {25833.259240236126, 1313.2250678008254, 1.6737199242981662, -1796.5356041719028, -346.54201637344073, -255.6362803317558, -3506.5436012151827, 0.0, -9590.068047002482, 2596.747880573514, 5196.784562257076, 1671.8843736950305, -2073.2151253423526, 0.0, 620.3587962311846, 723.9926514676865, 754.7068520077013, 312.7135996643316}, {25938.47372369449, 0.0, 0.0, -1852.9508965020236, -351.99811279284995, -258.49727950404406, -3510.1329163005757, 0.0, -9596.228455104947, 2558.8954597106613, 5184.38209889228, 1694.4618500930924, -2027.0415200984262, 0.0, 593.7693694279039, 722.5864443544249, 752.461364475723, 324.55277605617084}};
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
        prevYPos = 0;
        prevXPos = 0;
        prevMarioMode = 2;
        firstTrial = false;
        //print2dArray(weights);
        System.out.println(Arrays.deepToString(weights));
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
        double reward = marioProgressScore + marioYProgress + marioModeScore;
        if(frames == 0)
        {
            if(Math.abs(observation.getMarioFloatPos()[0] - pastXPos) < 10)
            {
                reward += MARIO_STUCK_WEIGHT;
            }
            frames = NUM_FRAMES;
            pastXPos = observation.getMarioFloatPos()[0];
        }
        
        return reward;
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
    /*
    public boolean[] getAction(Environment observation){
        if(prevObv == null)
        { 
            prevObv = observation;
            prevAction = A_RIGHT_ACTION;
            setAction(A_RIGHT_ACTION);
            return action;
        }

    public void signalStatus(int status){
        System.out.println("AHHH WE DEAD :(");
    }


        //prevObv = observation;
        double[][] features = FeatureExtractor.extractFeatures(prevObv, prevAction);
        double curQ = calcQ(features, testWeights, prevAction);
        double maxActionVal = -10000000;
        int maxAction = 0;
        double reward = 0;
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++)
        {
            if(uselessAction(i, observation)){
                continue;
            }
            double[][] curFeatures = FeatureExtractor.extractFeatures(observation, i);
            double qValue = calcQ(curFeatures, testWeights, i);
            //System.out.println("Q = " + qValue);
            if(qValue >= maxActionVal) {
                maxActionVal = qValue;
                maxAction = i;
                reward = maxActionVal;
            }
        }
        prevAction = maxAction;
        int chosenAction = maxAction;
        setAction(chosenAction);
        prevObv = observation;

        return action;
    }*/

    private void jumpHigh(int frames){
        highJumping = true;
        jumpCounter = frames;
    }
    
    public boolean[] getAction(Environment observation)
    {
        /*if(GlobalOptions.getMarioComponent().getStatus() == Mario.STATUS_DEAD){
            System.out.println("HEEEEES DEEAAAAD!");
        }*/
        if(!highJumping){
            if(prevObv == null)
            { //first action
               
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
                //if(uselessAction(i, observation)){
                    //System.out.println("WE GOT A USELESS ACTION: " + i);
                    //continue;
                //}
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
            
            int pickRand = randGen.nextInt(randomRange);
            int chosenAction = maxAction;
            //System.out.println("MAX ACTION: " + maxAction);
            if(pickRand == 1 || pickRand == 2)
            {
                //System.out.println("RAND ACTION: " + chosenAction);
                chosenAction = randGen.nextInt(FeatureExtractor.NUM_ACTIONS);
                //System.out.println("RANDOM: " + chosenAction);
            } else{
                //System.out.println("MAX ACTION: " + chosenAction);
            }
            //System.out.println("REWARD: " + reward);
            if(FeatureExtractor.extractFeatures(observation, 0)[0][FeatureExtractor.MARIO_IN_FRONT_LEDGE] == 1 /*|| features[prevAction][FeatureExtractor.MARIO_CLOSE_TO_LEDGE] == 1)*/ && observation.mayMarioJump()){
                jumpHigh(10);
                System.out.println("MEEP");
            }
            prevObv = observation;
            printWeights(weights);
            setAction(chosenAction);
            
            frames --;
            prevAction = chosenAction;
        } else{
            jumpCounter --;
            if(jumpCounter <= 0){
                highJumping = false;
            }
            setAction(A_RIGHT_ACTION);
            System.out.println("JUMPING : " + jumpCounter);
        }
            

        return action;
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
