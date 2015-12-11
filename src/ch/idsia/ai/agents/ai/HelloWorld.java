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

    private static final double MODE_WEIGHT = 20;
    private static final double PROGRESS_WEIGHT = 0.5;
    private static final double Y_PROGRESS_WEIGHT = 1;
    private static final double MARIO_STUCK_WEIGHT = -5;
    private static final double KILLS_WEIGHT = 3;
    private static final double DEATH_WEIGHT = -40;

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
    private int kills = 0;
    private boolean dead = false;

    private boolean training = true;

    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];
    private double[][] testWeights = new double[][] {{45.57383296976472, 29.534464345326, 16.845197423818767, 23.929827960774304, 588.5069611319284, 0.0, 0.0, 0.0, 2066.05781569796, 38.98087553938345, 13.56657589045513, 0.0, 20.553540273178765, 43.702747599143095, 0.0, 14.23173207564064, 21.899207271091257, 83.50363994958529, 62.599258387855535, 0.0, 10.707467633343828, 110.76664756131507, 74.70628806068557, 32.21370578810161, 52.505413388797194}, {68.60223428847445, 57.35950798197283, 37.92849578215348, 53.19167765461825, 631.9727794073075, 0.0, 0.0, 0.0, 2200.512988301533, 62.10890864836467, 8.278643035888672, 18.48114743260394, 13.23413651959029, 69.07403214139683, 7.277551042832039, 5.141970062255858, 16.496765136718746, 94.47253552501634, 62.46132125854488, 0.0, 30.977664184570255, 120.06235797796793, 67.10964857163151, 21.610830700386284, 34.100162588874745}, {56.0, 66.5061345881998, 31.873031049035376, 25.959097509664723, 538.1761668465062, 0.0, 0.0, 0.0, 2152.8287042878787, 58.03699844567802, 6.451745399840593, 23.567572237811852, 3.6470397949218754, 62.42894432918687, 23.20374961946225, 17.212084961625372, 23.17222129919903, 77.18501885880632, 32.9780716826498, 0.0, 14.430882289313898, 62.17127779529318, 86.39042714321101, 35.06626396385968, 51.62291295336167}, {56.0, 39.72604311075999, 31.950491271096922, 14.078405699912343, 569.8523535243762, 0.0, 0.0, 0.0, 2138.002940635243, 83.88369143208499, 7.65861389449833, 19.082444021319407, 7.926506831610573, 41.33033697543636, 9.137630434673124, 26.588693426835587, 14.615007716616105, 102.81117669200881, 57.30569199167422, 0.0, 12.975471010865654, 122.20502868623667, 47.99557709811041, 64.7147321889266, 16.494901167626097}, {132.0, 56.47772883824768, 35.617494150938015, 18.275648561625967, 592.4804628199311, 0.0, 0.0, 0.0, 2147.5071580843032, 67.84173003154456, 14.616618999938403, 0.0, 16.96810436212094, 70.44387587556655, 19.964414840908194, 16.51596907210544, 20.011989389468035, 103.30632354268177, 45.32976699289529, 0.0, 22.354011401630334, 122.9006650286536, 103.11267387002698, 18.44258803726607, 59.96302626182869}};



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

    public void signalStatus(int status){
        if(status == Mario.STATUS_DEAD){
            System.out.println("DEAD");
            dead = true;
            incremementWeights(reward(prevObv), prevAction); //sketch
        }

        System.out.println(Arrays.deepToString(weights));

    }

    public void reset()
    {
        System.out.println("STARTING");
        action = new boolean[Environment.numberOfButtons];// Empty action
        //System.out.println("RESET Weights: ");
        //print2dArray(weights);
        prevYPos = 0;
        prevXPos = 0;
        prevMarioMode = 2;
        firstTrial = false;
        prevObv = null;
        highJumping = false;
        kills = 0;
        dead = false;
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
        //System.out.println("Setting Action: " + actionNum);
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
        if(dead){
            return DEATH_WEIGHT;
        }
        double marioModeScore = (observation.getMarioMode() - prevMarioMode) * MODE_WEIGHT;
        if(observation.getMarioMode() != prevMarioMode)
        {
            System.out.println("MODE CHANGE WOW: ");
        }
        double killsScore = (observation.getKillsTotal() - kills) * KILLS_WEIGHT;
        kills = observation.getKillsTotal();
        double marioProgressScore = (observation.getMarioFloatPos()[0] - prevXPos - 1) * PROGRESS_WEIGHT;
        if(prevXPos > observation.getMarioFloatPos()[0]){
            marioProgressScore *= 3;
        }
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
        double reward = marioProgressScore + marioYProgress + marioModeScore + killsScore;
        if(frames == 0)
        {
            if(Math.abs(observation.getMarioFloatPos()[0] - pastXPos) < 10)
            {
                reward += MARIO_STUCK_WEIGHT;
                System.out.println("STUCK");
            }
            frames = NUM_FRAMES;
            pastXPos = observation.getMarioFloatPos()[0];
        }

        System.out.println("Progress Score: " + marioProgressScore);
        System.out.println("Y Progress score: " + marioYProgress);
        System.out.println("Mode score: " + marioModeScore);
        System.out.println("killsScore: " + killsScore);
        System.out.println("reward: " + reward);
        System.out.println("__________________________________");
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
        System.out.println("Incrementing action: " + action);
        double[][] features = FeatureExtractor.extractFeatures(prevObv, action);
        for(int i = 0; i < FeatureExtractor.NUM_FEATURES; i++){
            if(training){
                weights[action][i] -= features[action][i] * error * STEP_SIZE;
            } else{
                testWeights[action][i] -= features[action][i] * error * STEP_SIZE;
            }
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
        //System.out.println("_____________________________");
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
        double vOpt = -10000000;
        int maxAction = 0;
        double reward = 0;
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++)
        {
            if(uselessAction(i, observation)){
                //System.out.println("USELESS: " + i);
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
            //System.out.println("REWARD FOR ACTION " + i + " is: " + qValue);
            if(qValue >= vOpt) {
                
                vOpt = qValue;
                maxAction = i;
                reward = vOpt;
            }
        }
        double error = curQ - (reward(observation) + DISCOUNT * vOpt);
        /*MERP MERP*/
        //if(training){
            incremementWeights(error, prevAction);
        //}
        
        int pickRand = randGen.nextInt(randomRange);
        int chosenAction = maxAction;
        //System.out.println("MAX ACTION: " + maxAction);
        if(pickRand == 1)
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
