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
    private static final double Y_PROGRESS_WEIGHT = 0.5;
    private static final double MARIO_STUCK_WEIGHT = -5;
    private static final double KILLS_WEIGHT = 3;
    private static final double DEATH_WEIGHT = -40;

    private Environment prevObv;
    private int prevAction;
    private double prevXPos = 0;
    private int prevMarioMode = 2;
    private double prevYPos = 0;
    private int randomRange = 1000;
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
    private double[][] testWeights = new double[][] {{5167.707750760704, 0.0, 1964.8641531964795, 1675.6379040625081, 7805.750123839096, 0.0, 0.0, 0.0, 773958.146126918, 4877.75795662662, 1066.0103866142906, 1351.355646768644, 1037.830428294752, 7224.243618476473, 539.8151927719979, 1798.008305504232, 612.392832251863, 10995.971135090193, 2323.0152511461556, 0.0, 1989.4027557226518, 10587.235138656366, 8153.491875570029, 2282.8855984236993, 5095.655594982922}, {5156.259502314331, 7.604702097317205, 1944.9949314382682, 1690.7511234655374, 7809.623614950978, 0.0, 0.0, 0.0, 774006.793719388, 4879.077571116368, 1083.1873467528703, 1346.94110877294, 1064.9145939568514, 7230.671893089562, 532.9493682736481, 1813.3426796126867, 606.61801891318, 10985.50831527004, 2324.024212041749, 0.0, 1991.5918859322696, 10596.793764596405, 8176.755157204338, 2282.513142998985, 5096.385504003998}, {128.0, 14.770480652997502, 1958.2369280512955, 1690.8145804985004, 7806.717090815966, 0.0, 0.0, 0.0, 773982.4609969762, 4893.3429920640265, 1054.5531625188967, 1325.822974332433, 1060.471679409674, 7227.297474958406, 538.1207578920454, 1805.4761829065985, 608.1369459495917, 10981.957481022017, 2314.373615152808, 0.0, 1948.4913509706478, 10585.361231407951, 8197.305195213272, 2278.392675063173, 5090.4367179006695}, {184.0, 0.0, 1970.9523874388294, 1688.411596744327, 7809.655592432543, 0.0, 0.0, 0.0, 773993.8885644437, 4893.302085998135, 1025.5317083784912, 1310.8132343474124, 1038.2167583585056, 7231.007989659881, 514.6173625144806, 1835.2039368573578, 617.2940185951821, 10980.922578761374, 2315.020634463138, 0.0, 1962.351693787574, 10591.187376709371, 8179.876484140771, 2299.772310489712, 5061.223844639019}, {284.0, 3.618642697588075, 1963.0743002013505, 1687.0344440888489, 7809.336736979316, 0.0, 0.0, 0.0, 773988.8551731815, 4891.449878306816, 1027.778137052755, 1314.6605402539872, 1082.6022674887734, 7233.607412128661, 533.6024870931385, 1818.8578948227923, 610.1637555127472, 10993.594812763697, 2325.0892300463956, 0.0, 1986.0733776188663, 10593.072960033976, 8199.199243920977, 2280.1216429637684, 5086.194476327831}};

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
