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



    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];
    private double[][] testWeights = new double[][]{{1.8329764202878947E166, 2.531909143061707E165, 0.0, 0.0, 1.3604313729517781E161, 8.1414913304970635E158, 5.389838708571774E165, 0.0, 5.382516525280311E165, 0.0, 0.0, 1.246903658919973E145, 3.3695452871385454E162, 0.0, 2.4763205588331463E152, 1.5965451797921462E158, 6.1669248998428184E119, 3.1392616478851195E161}, {7.122413505377971E165, 2.9628638473520477E165, 0.0, 0.0, 1.0040943441629732E162, 8.141491330010997E158, 1.77073762616538E165, 0.0, 1.77311065941922E165, 0.0, 0.0, 1.466872291750193E145, 9.63384780565051E162, 0.0, 9.167511309658217E150, 2.6572405787575913E158, 2.2704994472729185E143, 2.3008304496404965E161}, {8.883203256943097E165, 3.287740720422594E165, 0.0, 0.0, 4.0080194922630023E121, 2.6313813989363045E135, 4.201921740683757E165, 0.0, 4.131331605853192E165, 0.0, 0.0, 2.620928728219933E145, 6.102987195130822E163, 0.0, 7.548308817654806E153, 2.4550773165243904E152, 3.0624926524627365E119, 1.77854035313215E159}, {8.410063988622071E165, 1.2518755197970267E165, 0.0, 0.0, 9.014244813560062E161, 1.1519530774787252E101, 3.354172848370213E165, 0.0, 3.294597664089294E165, 0.0, 0.0, 9.548011625824373E144, 6.052220378988367E163, 0.0, 5.973713360621957E152, 1.0611773896591331E158, 4.3663450909094585E142, 3.1638499807487665E154}, {7.063519870529907E165, 2.029234059973518E163, 0.0, 0.0, 7.902470431147037E161, 4.86067030785742E148, 1.7021444073036827E165, 0.0, 1.6963318329295808E165, 0.0, 0.0, 1.3748401384259513E145, 4.031346530465122E162, 0.0, 7.919709546442658E153, 3.595056277636721E153, 2.6198070545456748E143, 8.428311698241037E160}};




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
        prevXPos = observation.getMarioFloatPos()[0];
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
                //System.out.println("MEEP");
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
        //if(maxAction!= 2 && maxAction !=1) System.out.println("MAX ACTION: " + maxAction);
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
           // System.out.println("JUMPING : " + jumpCounter);
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
