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
    private double[][] testWeights = new double[][] {{0.0, 21.02196030560122, 7.263788754809914, 4.795181041485694, 18.541442411152538, 0.0, 0.0, 0.0, 977.1295672637729, 37.898024253050956, 0.811934202278853, 9.023103179486737, 0.0, 16.074827336086056, 2.0785636925814805, 1.8623884091655087, 1.034917882371519, 47.58090013890557, 19.589311040725434, 0.0, 19.70145413098965, 37.87722427611923, 12.26179312952545, 0.0, 4.232784778864423}, {46.26652756093645, 25.888493772192835, 15.144738540503083, 13.284960853094539, 26.43076473111949, 0.0, 0.0, 0.0, 1047.955551833952, 19.837828626101008, 4.941350833711645, 9.520832924099802, 6.520778701012649, 17.67347353957906, 3.8922376033972954, 5.777056173878981, 1.1854020408472872, 38.25208800902328, 14.608123493033396, 0.0, 7.350396512836941, 49.49614663877757, 33.35226483161372, 8.890158058657784, 17.72911078527119}, {72.0, 23.40488374697022, 21.781954731593444, 16.928863459227287, 26.725514185487928, 0.0, 0.0, 0.0, 1062.661362591742, 22.957979515173236, 0.0, 20.85279201974612, 13.728127748480555, 13.51260747762452, 0.1504267478390826, 10.511865188944675, 3.3919679926744206, 32.94703436713955, 20.935901693193177, 0.0, 0.7931811255510136, 44.9550503567118, 33.765778391469986, 5.353121055663008, 8.729847986403309}, {80.0, 11.057622313865572, 17.681216657717663, 8.965514578956576, 16.777954084185517, 0.0, 0.0, 0.0, 915.0928978579554, 22.794346793550304, 9.590838667953125, 10.955474760689151, 3.979998779296875, 20.296751436565824, 13.320322626933054, 21.32615193881436, 7.264304791078878, 47.414777619822175, 17.095748949240427, 0.0, 5.371875762802304, 52.9516376485019, 51.64604855331686, 13.189339427332323, 20.417904054711954}, {88.0, 26.384993648886788, 19.454716039230636, 7.6266468901425934, 24.212978134722903, 0.0, 0.0, 0.0, 987.6481164454875, 43.86291690014212, 1.9556431025873282, 1.662580677314142, 1.8365963349391679, 16.615364789590956, 3.2674236419990983, 19.642705769005232, 10.653985469829168, 52.06945672305455, 13.737137250952273, 0.0, 5.149741781052513, 55.862506515474664, 47.57148436515987, 5.013124687717406, 15.780205485597547}};



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
