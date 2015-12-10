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
    //private static final int A_ACTION = 0;
    private static final int LEFT_ACTION = 0;
    private static final int RIGHT_ACTION = 1;
    //private static final int A_LEFT_ACTION = 3;
    private static final int A_RIGHT_ACTION = 5; //this actually isn't stored in feature extractor, just called by other actions
    private static final int LONG_JUMP_ACTION = 2;
    private static final int MEDIUM_JUMP_ACTION = 3;
    private static final int SMALL_JUMP_ACTION = 4;
    private static final int NUM_FRAMES = 5;

    //private static final int NUM_ACTIONS = 5;
    //private static final int NUM_FEATURES = 12;
    private static final double DISCOUNT = 1;
    private static final double STEP_SIZE = 0.1;

    private static final double MODE_WEIGHT = 5;
    private static final double PROGRESS_WEIGHT = 0.2;
    private static final double Y_PROGRESS_WEIGHT = 0;
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

    private boolean training = true;

    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];
    private double[][] testWeights = new double[][]{{7443203.452177169, 6375929.130141851, 0.0, 0.0, 55450.536791966755, 350421.2752935597, 4138338.0949846506, 0.0, 3823537.6623416487, 0.0, 0.0, 66046.5449445536, 1210133.4547858397, 0.0, 93707.73610877377, 209964.89257364662, 23992.430919815994, 30986.114255481523}, {7396927.428823305, 6420896.110576321, 0.0, 0.0, 55495.974009936486, 355627.0253948655, 4103985.0020787073, 0.0, 3860033.559927329, 0.0, 0.0, 123884.06453609436, 1194613.3887976396, 0.0, 84284.55434941401, 210697.42383732996, 20052.243294802487, 30820.50000533416}, {7929774.0500113545, 1256231.5534669664, 0.0, 0.0, 503673.2080292855, 472354.0228287712, 4992321.770590185, 0.0, 5306607.830251044, 0.0, 0.0, 0.0, 0.0, 0.0, 30950.257935016627, 206591.85875709567, 774.8754187763052, 33432.18523532562}, {7925168.155928818, 1840421.707538376, 0.0, 0.0, 382313.2877606263, 486040.01337463234, 4972289.347719243, 0.0, 5339261.130313682, 0.0, 0.0, 0.0, 0.0, 0.0, 26397.216176235415, 205938.4019361382, 8633.524356083437, 28373.70353886045}, {7924742.907738333, 3544010.337182883, 0.0, 0.0, 303058.48013390036, 497204.5642173105, 4970492.147797707, 0.0, 5339677.118154684, 0.0, 0.0, 0.0, 0.0, 0.0, 26640.80530015986, 212118.32632085402, 2196.2472653896707, 25696.62169124722}};











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
        System.out.println(actionNum);
        switch(actionNum){
            /*case A_ACTION:
                action[Mario.KEY_JUMP] = true;
                break;*/
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
            /*case A_LEFT_ACTION:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                break;*/
            case LONG_JUMP_ACTION:
                jumpHigh(8);
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case MEDIUM_JUMP_ACTION:
                jumpHigh(4);
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case SMALL_JUMP_ACTION:
                jumpHigh(2);
                action[Mario.KEY_RIGHT] = true;
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
    private void getCloseToGround(Environment observation)
    {
        
    }

    public double reward(Environment observation){
        /*if(Mario.getStatus() == Mario.STATUS_DEAD){

        }*/
        double marioModeScore = (observation.getMarioMode() - prevMarioMode) * MODE_WEIGHT;
        if(observation.getMarioMode() != prevMarioMode)
        {
            System.out.println("MODE CHANGE WOW: ");
        }
        double marioProgressScore = (observation.getMarioFloatPos()[0] - prevXPos - 1) * PROGRESS_WEIGHT;
        prevMarioMode = observation.getMarioMode();
        
        double marioYProgress = (prevYPos - observation.getMarioFloatPos()[1]) * Y_PROGRESS_WEIGHT;

        if(observation.isMarioOnGround() && marioYProgress > 0)
        {
            prevYPos = observation.getMarioFloatPos()[1];
            //System.out.println("LALALAYAY:" + prevAction);
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
        if((action == LONG_JUMP_ACTION || action == SMALL_JUMP_ACTION || action == MEDIUM_JUMP_ACTION) && !observation.mayMarioJump()){
            return true;
        }
        return false;   
    }
    
    /*
    public boolean[] getAction(Environment observation){
        double curQ = 0;
        double maxActionVal = 0;
        int maxAction = 0;
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
            curQ = calcQ(features, weights, prevAction);
            maxActionVal = -10000000;
            maxAction = 0;
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
            //incremementWeights(error, prevAction);
            
            int pickRand = randGen.nextInt(randomRange);
            int chosenAction = maxAction;
            //System.out.println("MAX ACTION: " + maxAction);
            if(pickRand == 1 || pickRand == 2)
            {
                //System.out.println("RAND ACTION: " + chosenAction);
                //chosenAction = randGen.nextInt(FeatureExtractor.NUM_ACTIONS);
                //System.out.println("RANDOM: " + chosenAction);
            } else{
                //System.out.println("MAX ACTION: " + chosenAction);
            }

            //System.out.println("REWARD: " + reward);
            if(FeatureExtractor.extractFeatures(observation, 0)[0][FeatureExtractor.MARIO_IN_FRONT_LEDGE] == 1  && observation.mayMarioJump()){
                jumpHigh(8);
                setAction(A_RIGHT_ACTION);
                
                System.out.println("MEEP");
                return action;
            }
            prevObv = observation;
            printWeights(weights);
            setAction(chosenAction);
            
            frames --;
            prevAction = chosenAction;

        

        }
        else{
            jumpCounter --;
            if(jumpCounter <= 0){
                highJumping = false;
            }
            setAction(A_RIGHT_ACTION);
           // System.out.println("JUMPING : " + jumpCounter);
        }
            

        return action;
    }
    */

    private void jumpHigh(int frames){
        //System.out.println("GOING UUUUUP");
        highJumping = true;
        jumpCounter = frames;
    }
    
    
    public boolean[] getAction(Environment observation)
    {
        if(highJumping){
            jumpCounter --;
            //System.out.println("JUMPING : " + jumpCounter);
            if(jumpCounter <= 0){
                //highJumping = false;
                if(!observation.isMarioOnGround()){
                    setAction(RIGHT_ACTION);
                    prevXPos = observation.getMarioFloatPos()[0];
                    return action; 
                } else {
                    highJumping = false;
                    //System.out.println("LANDING MADE CAPTAIN");
                }
                
            } else{
                setAction(A_RIGHT_ACTION);
                prevXPos = observation.getMarioFloatPos()[0];

                return action;
            }
            
        }
        //System.out.println("WE NOT JUMPING");
        if(prevObv == null)
        { //first action
           
            prevObv = observation;
            prevAction = RIGHT_ACTION;
            setAction(RIGHT_ACTION);
            return action;
        }

        //prevObv = observation;
        double[][] features = FeatureExtractor.extractFeatures(prevObv, prevAction);
        /*MERP MERP*/

        double curQ = 0; 
        if(training)
        {
            curQ = calcQ(features, weights, prevAction);
        }else{
            curQ = calcQ(features, testWeights, prevAction);
        }
        double maxActionVal = -10000000;
        int maxAction = 0;
        double reward = 0;
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++)
        {
            if(uselessAction(i, observation)){
                continue;
            }
            double[][] curFeatures = FeatureExtractor.extractFeatures(observation, i);
            /*MERP MERP*/
            double qValue = 0;
            if(training){
                qValue = calcQ(curFeatures, weights, i);
            } else{
                qValue = calcQ(curFeatures, testWeights, i);
            }
            //System.out.println("Q = " + qValue);
            if(qValue >= maxActionVal) {
                maxActionVal = qValue;
                maxAction = i;
                reward = maxActionVal;
            }
        }
        double error = curQ - (reward(observation) + DISCOUNT * maxActionVal);
        /*MERP MERP*/
        if(training){
            incremementWeights(error, prevAction);
        }
        
        int pickRand = randGen.nextInt(randomRange);
        int chosenAction = maxAction;
        //System.out.println("MAX ACTION: " + maxAction);
        if(pickRand == 1 || pickRand == 2)
        {
            //System.out.println("RAND ACTION: " + chosenAction);
            /*MERRRRRRP*/
            if(training){
                chosenAction = randGen.nextInt(FeatureExtractor.NUM_ACTIONS);
            }
            //System.out.println("RANDOM: " + chosenAction);
        } else{
            //System.out.println("MAX ACTION: " + chosenAction);
        }

        //System.out.println("REWARD: " + reward);
        /*if(FeatureExtractor.extractFeatures(observation, 0)[0][FeatureExtractor.MARIO_IN_FRONT_LEDGE] == 1  && observation.mayMarioJump()){
            jumpHigh(8);
            setAction(A_RIGHT_ACTION);
            return action;
            //System.out.println("MEEP");
        }*/
        prevXPos = observation.getMarioFloatPos()[0];
        prevObv = observation;
        printWeights(weights);
        setAction(chosenAction);
        
        frames --;
        prevAction = chosenAction;

    

            

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
