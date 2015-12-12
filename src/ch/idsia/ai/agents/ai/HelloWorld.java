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
    private static final double PROGRESS_WEIGHT = 0.3;
    private static final double Y_PROGRESS_WEIGHT = 0.3;
    private static final double MARIO_STUCK_WEIGHT = -5;
    private static final double KILLS_WEIGHT = 10;
    private static final double DEATH_WEIGHT = -40;
    private static final double JUMP_X_BIAS = 0;

    private Environment prevObv;
    private int prevAction;
    private double prevXPos = 0;
    private int prevMarioMode = 2;
    private double prevYPos = 0;

    private int randomRange = 3;

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
    private boolean testRandom = false;

    private double[][] weights = new double[FeatureExtractor.NUM_ACTIONS][FeatureExtractor.NUM_FEATURES];
    private double[][] testWeights = new double[][] {{760.0, 0.0, 19160.315272142147, 10674.53444846348, 5598.983186456777, -2.080807096562057, 0.0, 0.0, 54948.39587295554, 2200.656100741232, 700.6787686776611, 1717.685207245195, 1338.2079902985727, 2384.9251092835516, 1230.9332462393802, 2588.1321500441063, 1151.8472012956256, 2671.0305915147464, 2275.1240827676997, 0.0, 1887.2683019611582, 3449.9760054272983, 4020.3568760893913, 1854.1546320936245, 2201.232876738607, 3831.53894758956, 6523.244304809476, 2933.233281276484, 0.0, 0.0, 0.0, 20672.732703423855, 24329.232700768254, 23950.038659714904, 912.963698606022}, {9508.0, 0.0, 19153.281020861723, 10676.416842951516, 5598.938509305645, 0.0, 0.0, 0.0, 54967.33905647854, 2203.3021014229444, 692.3136909804945, 1716.7477239441343, 1335.7683502039752, 2385.3340661663897, 1223.5439418741485, 2617.6597470515712, 1150.3860723364824, 2664.6304648361524, 2284.10375941141, 0.0, 1887.6371667185756, 3446.6095975426038, 4014.5156356007133, 1860.1546512204568, 2196.15887233455, 3824.5148594231596, 6524.262558770622, 2933.0638551732004, 0.0, 0.0, 0.0, 20729.245091683515, 24343.79326568022, 23974.769168589413, 915.239661398144}, {21204.0, 0.0, 19148.282243103375, 10680.355633493056, 5592.17387879251, 0.0, 0.0, 0.0, 54963.1765256437, 2203.4940417960474, 695.5871086377437, 1716.3081423038825, 1338.284769614742, 2378.969662232827, 1231.0965673316105, 2615.531496308607, 1156.2738689624364, 2668.073308577389, 2275.851583610201, 0.0, 1881.817601938266, 3437.930210436119, 4020.435480563153, 1852.9498895319548, 2197.6286883895755, 3830.1213381334856, 6527.894532507662, 2931.7477431437724, 0.0, 0.0, 0.0, 20723.146092826675, 24339.524707718836, 23969.961284870085, 913.1876761907213}, {9412.0, 0.0, 19155.712342786952, 10677.564891663185, 5601.617012851565, 0.0, 0.0, 0.0, 54968.51947668492, 2200.420145537588, 691.9513829827126, 1732.6824146695926, 1337.1220570244943, 2384.744313518387, 1226.9150742608192, 2623.5151662556323, 1154.449330001786, 2670.1306774664386, 2285.3278330131207, 0.0, 1887.2073832148199, 3444.8984371926053, 4011.2040889406476, 1871.7332375086585, 2200.0936326952765, 3831.231430177556, 6523.931466400785, 2936.5782652717603, 0.0, 0.0, 0.0, 20707.325843616232, 24346.313695648707, 23962.920683768527, 914.832195068038}, {5496.0, 0.0, 19153.749335345216, 10671.86108843828, 5596.809425322022, 0.0, 0.0, 0.0, 54963.91905442815, 2213.781288810339, 703.1250389458713, 1718.0841103686294, 1338.4205899980027, 2386.005642951394, 1226.472271216885, 2615.4970634870338, 1152.7817696290447, 2670.558326158081, 2276.2219797566436, 0.0, 1888.181183479269, 3443.986128434003, 4018.9827585361686, 1861.6269135878322, 2196.553415182503, 3829.2556964074706, 6527.747190965098, 2939.064076960316, 0.0, 0.0, 0.0, 20708.16339689141, 24346.02834236068, 23974.154014144788, 913.755545455125}};

    public HelloWorld(String s)
    {

        super("HelloWorld");
        for(int i = 0; i < FeatureExtractor.NUM_ACTIONS; i++){
            for(int j = 0; j < FeatureExtractor.NUM_FEATURES; j++){
                weights[i][j] = 0;
            }
        }
        //System.out.println("MEOWWW Weights: ");
        print2dArray(weights);
        
    }

    public void signalStatus(int status){
        if(testRandom){
            return;
        }
        if(status == Mario.STATUS_DEAD){
            //System.out.println("DEAD");
            dead = true;
            incremementWeights(reward(prevObv), prevAction); //sketch
        }


        System.out.println(Arrays.deepToString(weights));

    }

    public void reset()
    {
        //System.out.println("STARTING");
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
        if(training){
            //System.out.println(Arrays.deepToString(weights));
        }
        
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
            //System.out.println(a);
        }
    }

    private void print1dArray(double[] arr){
        int len = arr.length;
        String a = "";
        for(int i = 0; i < len; i ++){
            a += arr[i] + "  ";
        }
        //System.out.println(a);
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
            //System.out.println("MODE CHANGE WOW: ");
        }
        double killsScore = (observation.getKillsByStomp() - kills) * KILLS_WEIGHT;
        kills = observation.getKillsByStomp();
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
                //System.out.println("STUCK");
            }
            frames = NUM_FRAMES;
            pastXPos = observation.getMarioFloatPos()[0];
        }

        //System.out.println("Progress Score: " + marioProgressScore);
        //System.out.println("Y Progress score: " + marioYProgress);
        //System.out.println("Mode score: " + marioModeScore);
        //System.out.println("killsScore: " + killsScore);
        //System.out.println("reward: " + reward);
        //System.out.println("__________________________________");
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
    
    private void jumpHigh(int frames){
        //System.out.println("GOING UUUUUP");
        highJumping = true;
        jumpCounter = frames;
    }
    
    
    public boolean[] getAction(Environment observation)
    {
        //System.out.println("_____________________________");
        //System.out.println("WE RANDOM?:" + testRandom);
        if(testRandom){
            Random randAction = new Random();
            int pickRand = randAction.nextInt(FeatureExtractor.NUM_ACTIONS);
            setAction(pickRand);
            //System.out.println("GET TO ACTiON?:" + pickRand);
            return action;

        }
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

        //hold actions while in air, so that prev obv stays how it was
        if(!observation.isMarioOnGround()){
            prevXPos = observation.getMarioFloatPos()[0] + JUMP_X_BIAS;
            setAction(prevAction);
            //System.out.println("HOOOOOOLD");
            return action;
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
        if(training){
            incremementWeights(error, prevAction);
        }
        
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
