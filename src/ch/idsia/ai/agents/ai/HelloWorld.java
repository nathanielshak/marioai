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
    private static final double Y_PROGRESS_WEIGHT = 100;
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
    private double[][] testWeights = new double[][]{{143753.28099996227, 27439.72285336339, -0.2329998016357422, 0.0, -1618.8985721684187, -1702.8778035597015, -26361.806154145084, 0.0, -70609.51263672263, 0.0, 0.0, 10589.556032207158, -10585.711682434894, 0.0, 6209.896178482133, 4953.649658156672, 4338.739232116856, 1704.2986533274366}, {143820.02625610307, 122106.47921696362, 7.368601365137147, 0.0, -1618.033040167786, -1579.2646764400642, -26388.955724932246, 0.0, -70637.10543114341, 0.0, 0.0, 10587.21451926444, -10574.218867572272, 0.0, 6237.0860434544475, 4963.953395992842, 4342.431044980206, 1646.2167004653986}, {143911.35247530063, 122185.75504026703, 1.8343210220336914, 0.0, -1626.1250803380815, -1589.0278030901998, -26392.713306254318, 0.0, -70692.9453611347, 0.0, 0.0, 10614.051273539617, -10500.635622979133, 0.0, 6208.879251441145, 4941.549039948, 4360.835558221862, 1662.9815150360582}, {143717.77965206478, 47314.17032533635, 0.30691290616814515, 0.0, -1626.8232826212611, -1589.8857810741647, -26372.80286192055, 0.0, -70600.64503037151, 0.0, 0.0, 10583.87367337172, -10544.436742801723, 0.0, 6180.084153058643, 4914.488849077498, 4322.486760295451, 1648.8139860627477}, {143662.93315557425, 33824.64796400747, 17.542394410588894, 0.0, -1629.3820055767244, -1593.873830623819, -26388.957282339157, 0.0, -70602.75312889557, 0.0, 0.0, 10591.247727989678, -10602.697629397051, 0.0, 6218.458783048232, 4899.29968177897, 4318.133796235348, 1673.6996067709847}};





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
    private getCloseToGround(Environment observation)
    {
        
    }

    public double reward(Environment observation){
        /*if(Mario.getStatus() == Mario.STATUS_DEAD){

        }*/
        double marioModeScore = (observation.getMarioMode() - prevMarioMode) * MODE_WEIGHT;
        double marioProgressScore = (observation.getMarioFloatPos()[0] - prevXPos - 1) * PROGRESS_WEIGHT;
        prevMarioMode = observation.getMarioMode();
        prevXPos = observation.getMarioFloatPos()[0];
        double marioYProgress = (prevYPos - observation.getMarioFloatPos()[1]) * Y_PROGRESS_WEIGHT;

        if(observation.isMarioOnGround() && marioYProgress > 0)
        {
            prevYPos = observation.getMarioFloatPos()[1];
            System.out.println("LALALAYAY:" + prevAction);
        } else if(observation.isMarioOnGround() && marioYProgress <= 0){
            marioYProgress = 0;
            prevYPos = observation.getMarioFloatPos()[1];
        }else{
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

        }
        double error = curQ - (reward(observation) + DISCOUNT * maxActionVal);
        incremementWeights(error, prevAction);
        
        int pickRand = randGen.nextInt(randomRange);
        int chosenAction = maxAction;
        if(maxAction!= 2 && maxAction !=1) System.out.println("MAX ACTION: " + maxAction);
        if(pickRand == 1 || pickRand == 2)
        {
            //System.out.println("RAND ACTION: " + chosenAction);
            chosenAction = randGen.nextInt(FeatureExtractor.NUM_ACTIONS);
            //System.out.println("RANDOM: " + chosenAction);

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
