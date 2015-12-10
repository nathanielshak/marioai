package ch.idsia.ai.agents;
import ch.idsia.mario.environments.Environment;
import java.awt.Point;
import java.util.Arrays;




public class FeatureExtractor {

	//Objects
	private static final Point MARIO_LOCATION = new Point(11,11);
	private static final int LEDGE = -10;
	private static final int PIPE = 2;
	private static final int FLOATING_LEDGE = -11;
	private static final int SMALL_LEDGE_RANGE = 1;
	private static final int MEDIUM_LEDGE_RANGE = 2;
	private static final int CLOSE_TO_LEDGE_BEGIN = 2;
	private static final int CLOSE_TO_LEDGE_END = 4;

	//Features
	private static final int NUM_ACTIONS = 5;
	private static final int NUM_FEATURES =13;

	//Features Indices
	private static final int ON_GROUND = 0;
	private static final int CAN_JUMP = 1;
	private static final int DANGER_OF_GAP = 2;
	private static final int MARIO_FACING_LEDGE = 3;
	private static final int SMALL_LEDGE = 4;
	private static final int MEDIUM_LEDGE = 5;
	private static final int LARGE_LEDGE = 6;
	private static final int MARIO_IS_STUCK = 7;
	private static final int MARIO_IN_FRONT_LEDGE = 8;
	private static final int MARIO_CLOSE_TO_LEDGE = 9;
	private static final int MARIO_NOT_CLOSE_TO_LEDGE = 10;
	private static final int IN_FRONT_OF_LEDGE_JUMPED = 11;
	private static final int AIR_FACING_LEDGE = 12;


	//Stuff *Come back and name this*
	private static float prevMarioPos = 0;

	private static void print2dLevel(byte[][] arr){
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

	 private static void print2dArray(double[][] arr){
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

	private static boolean DangerOfGap(byte[][] levelScene)
    {
        for (int x = 9; x < 13; ++x)
        {
            boolean f = true;
            for(int y = 12; y < 22; ++y)
            {
                if  (levelScene[y][x] != 0)
                    f = false;
            }
            if (f && levelScene[12][11] != 0)
                return true;
        }
        return false;
    }

    private static void printLevel(byte[] levelScene){
    	for(int i = 0; i < levelScene.length; i++)
    	{
    		System.out.print(levelScene[i]);
    	}
    	System.out.print("\n");
    }

    private static boolean FacingLedge(byte[][] levelScene){
    	for(int x = MARIO_LOCATION.x + 1; x < levelScene[0].length; x++)
    	{
    		if(levelScene[MARIO_LOCATION.y][x] == LEDGE || levelScene[MARIO_LOCATION.y][x] == PIPE)
    		{
    			return true;
    		}
    	}

    	return false;
    }

    private static int LedgeRange(byte[][] levelScene){
    	for(int x = MARIO_LOCATION.x + 1; x < levelScene[0].length; x++)
    	{
    		int yPos = 0;
    		if(levelScene[MARIO_LOCATION.y][x] == LEDGE || levelScene[MARIO_LOCATION.y][x] == PIPE)
    		{
    			for(int y = MARIO_LOCATION.y; y > MARIO_LOCATION.y - 4; y--)
    			{
    				if(levelScene[x][y] == LEDGE || levelScene[x][y] == PIPE)
    				{
    					yPos ++;
    				}
    				
    			}
    			return yPos;

    		}
    	}
    	return 0;
    }

    private static boolean MarioIsStuck(float currXPos, boolean facingLedge){
    	return (prevMarioPos == currXPos) & facingLedge;
    }

    private static int DistanceToLedge(byte[][] levelScene, boolean facingLedge){
    	if(!facingLedge)
    	{
    		return 0;
    	}
    	if(levelScene[MARIO_LOCATION.y][MARIO_LOCATION.x + 1] == LEDGE || levelScene[MARIO_LOCATION.y][MARIO_LOCATION.x + 1] == PIPE)
    	{
    		return 1;
    	}
    	for(int x = MARIO_LOCATION.x + CLOSE_TO_LEDGE_BEGIN; x < (MARIO_LOCATION.x + CLOSE_TO_LEDGE_END); x++)
    	{
    		if(levelScene[MARIO_LOCATION.y][x] == LEDGE || levelScene[MARIO_LOCATION.y][x] == PIPE)
    		{
    			return 2;
    		}
    	}
    	return 3;
    }

    private static boolean inFrontOfLedgeJumped(Environment observation, boolean facingLedge, byte[][] levelScene){
    	if(observation.isMarioOnGround() || facingLedge){
    		return false;
    	}
    	int ledgeBelow = levelScene.length;
    	int ledgeInFront = levelScene.length;

    	for(int y = MARIO_LOCATION.y - 2; y < levelScene.length; y++){
    		if(levelScene[y][MARIO_LOCATION.x] == LEDGE || levelScene[y][MARIO_LOCATION.x] == PIPE){
    			ledgeBelow = y;
    			break;
    		}
    	}
    	for(int xOffset = 1; xOffset <= 3; xOffset++){
	    	for(int y = MARIO_LOCATION.y - 2; y < levelScene.length; y++){
	    		if(levelScene[y][MARIO_LOCATION.x + xOffset] == LEDGE || levelScene[y][MARIO_LOCATION.x + xOffset] == PIPE){
	    			if(y < ledgeInFront){
	    				ledgeInFront = y;
	    			}
	    			break;
	    		}
	    	}
		}
    	if (ledgeInFront < ledgeBelow){
    		System.out.println("AHAHAHAHKDFJDSJGFDSKNGERKLSGSDKLFHNSDLGKNSDKNGV");
    	}
    	return (ledgeInFront < ledgeBelow);
    }

	public static double[][] extractFeatures(Environment observation, int action) {
		double[][] features = new double[NUM_ACTIONS][NUM_FEATURES];
		for(int curAction = 0; curAction < NUM_ACTIONS; curAction++)
		{
			if (action != curAction)
			{
				for(int curFeature = 0; curFeature < NUM_FEATURES; curFeature++)
				{
					features[curAction][curFeature] = 0;
				}
			}
		}
		//populate features of taken action column
		features[action][ON_GROUND] = observation.isMarioOnGround() ? 1 : 0;
		features[action][CAN_JUMP] = observation.mayMarioJump() ? 1 : 0;
		byte [][] levelScene = observation.getLevelSceneObservation();
		features[action][DANGER_OF_GAP] = DangerOfGap(levelScene) ? 1 : 0;

		//Ledge Features
		boolean facingLedge = FacingLedge(levelScene);
		features[action][MARIO_FACING_LEDGE] =  facingLedge ? 1 : 0;
		features[action][SMALL_LEDGE] = LedgeRange(levelScene) == SMALL_LEDGE_RANGE ? 1: 0;
		features[action][MEDIUM_LEDGE] = LedgeRange(levelScene) == MEDIUM_LEDGE_RANGE ? 1: 0;
		features[action][LARGE_LEDGE] = LedgeRange(levelScene) > MEDIUM_LEDGE_RANGE ? 1: 0;
		float currXPos = observation.getMarioFloatPos()[0];
		features[action][MARIO_IS_STUCK] = MarioIsStuck(currXPos, facingLedge) ? 1: 0;

		//Ledge distance
		
		features[action][MARIO_IN_FRONT_LEDGE] = DistanceToLedge(levelScene, facingLedge) == 1 ? 1:0;
		features[action][MARIO_CLOSE_TO_LEDGE] = DistanceToLedge(levelScene, facingLedge) == 2 ? 1:0;
		features[action][MARIO_NOT_CLOSE_TO_LEDGE] = DistanceToLedge(levelScene, facingLedge) == 3 ? 1:0;

		//Jumped facing ledge
		features[action][IN_FRONT_OF_LEDGE_JUMPED] = inFrontOfLedgeJumped(observation, facingLedge, levelScene) ? 1:0;

		//In air facing ledge
		features[action][AIR_FACING_LEDGE] = (facingLedge && !observation.isMarioOnGround()) ? 1:0;
		System.out.println("FACING LEDGE JUMPED");

		//System.out.print("Printing Level\n");
		//print2dArray(features);
		//System.out.print("\n\n\n\n");
		//print2dLevel(levelScene);
		//System.out.print("\n\n\n\n");

		return features;
	}



}