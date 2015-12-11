package ch.idsia.ai.agents;
import ch.idsia.mario.environments.Environment;
import java.awt.Point;
import java.util.Arrays;




public class FeatureExtractor {

	//Objects
	private static final Point MARIO_LOCATION = new Point(11,11);
	private static final int LEDGE = -10;
	private static final int PIPE = 20;
	private static final int FLOATING_LEDGE = -11;
	private static final int SMALL_LEDGE_RANGE = 1;
	private static final int MEDIUM_LEDGE_RANGE = 2;
	private static final int LARGE_LEDGE_RANGE = 3;
	private static final int CLOSE_TO_LEDGE_BEGIN = 2;
	private static final int CLOSE_TO_LEDGE_END = 4;
	private static final int CLOSE_ENEMY_RANGE = 2;

	//Features
	public static final int NUM_ACTIONS = 5;
	public static final int NUM_FEATURES = 35;

	//Features Indices
	//public static final int ON_GROUND = 0;
	//public static final int CAN_JUMP = 1;
	public static final int IN_FRONT_OF_LEDGE_JUMPED = 0;
	public static final int DANGER_OF_GAP = 1;
	//public static final int MARIO_FACING_LEDGE = 2;
	public static final int SMALL_LEDGE = 2;
	public static final int MEDIUM_LEDGE = 3;
	public static final int LARGE_LEDGE = 4;
	public static final int LARGER_LEDGE = 34;
	public static final int MARIO_IS_STUCK = 5;
	public static final int MARIO_IN_FRONT_LEDGE = 6;
	public static final int MARIO_CLOSE_TO_LEDGE = 7;
	public static final int MARIO_NOT_CLOSE_TO_LEDGE = 8;
	
	//public static final int AIR_FACING_LEDGE = 11;

	//Enemy Indices
	
	//close features             
	/* Quadrants:
		4|3|2	
		5| |1
		6|7|8	*/
	public static final int Q1_CLOSE = 9;
	public static final int Q2_CLOSE = 10;
	public static final int Q3_CLOSE = 11;
	public static final int Q4_CLOSE = 12;
	public static final int Q5_CLOSE = 13;
	public static final int Q6_CLOSE = 14;
	public static final int Q7_CLOSE = 15;
	public static final int Q8_CLOSE = 16;

	//medium far features
	public static final int Q1_MED = 17;
	public static final int Q2_MED = 18;
	public static final int Q3_MED = 19;
	public static final int Q4_MED = 20;
	public static final int Q5_MED = 21;
	public static final int Q6_MED = 22;
	public static final int Q7_MED = 23;
	public static final int Q8_MED = 24;

	//farther features forward
	public static final int Q1_FAR = 25;
	public static final int Q2_FAR = 26;
	public static final int Q3_FAR = 27;

	//even farther features forward
	public static final int Q1_FARTHER = 28;
	public static final int Q2_FARTHER = 29;
	public static final int Q3_FARTHER = 30;

	//gaps
	public static final int GAP_CLOSE = 31;
	public static final int GAP_MED = 32;
	public static final int GAP_FAR = 33;
	//larger ledge is 34

	//Stuff *Come back and name this*
	private static float prevMarioPos = 0;
	private static boolean detectY = false;

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

    public static boolean gapExists(byte[][] levelScene, int beginX, int endX){
    	for(int x = beginX; x <= endX; x ++){
    		boolean columnGap = true;
    		for(int y = 12; y < 22; y ++ ){
    			if(levelScene[y][x] != 0){
    				columnGap = false;
    				break;
    			}
    		}
    		if(columnGap){
    			return true;
    		}
    	}
    	return false;
    }


	private static boolean DangerOfGap(byte[][] levelScene)
    {
        for (int x = 11; x < 12; ++x)
        {
            boolean f = true;
            for(int y = 12; y < 22; ++y)
            {
                if  (levelScene[y][x] != 0){
                    f = false;
                }
            }
            if (f && levelScene[12][11] != 0){
                return true;
            }
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

    private static int distanceToLedge(byte[][] levelScene){
    	for(int x = MARIO_LOCATION.x + 1; x < levelScene[0].length; x++)
    	{
    		if(levelScene[MARIO_LOCATION.y][x] == LEDGE || levelScene[MARIO_LOCATION.y][x] == PIPE)
    		{
    			return x;
    		}
    	}

    	return -1;
    }

    private static int LedgeRange(byte[][] levelScene){
    	for(int x = MARIO_LOCATION.x + 1; x < levelScene[0].length; x++)
    	{
    		int yPos = 0;
    		if(levelScene[MARIO_LOCATION.y][x] == LEDGE || levelScene[MARIO_LOCATION.y][x] == PIPE)
    		{
    			for(int y = MARIO_LOCATION.y; y > MARIO_LOCATION.y - 7; y--)
    			{
    				if(levelScene[y][x] == LEDGE || levelScene[y][x] == PIPE)
    				{
    					//System.out.println("LEDGE AT: " + x + " y: " + y);
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

/*
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
*/

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
    		//System.out.println("AHAHAHAHKDFJDSJGFDSKNGERKLSGSDKLFHNSDLGKNSDKNGV");
    	}
    	return (ledgeInFront < ledgeBelow);
    }

    public static void setEnemyClose(byte[][] enemyScene, double[][] features, int action){
    	int x = MARIO_LOCATION.x;
    	int y = MARIO_LOCATION.y;
    	if(enemyScene[y][x + 1] != 0){
    		features[action][Q1_CLOSE] = 1;
    	}
    	if(enemyScene[y +1][x + 1] != 0){
    		features[action][Q2_CLOSE] = 1;
    	}
    	if(enemyScene[y + 1][x] != 0){
    		features[action][Q3_CLOSE] = 1;
    	}
    	if(enemyScene[y + 1][x - 1] != 0){
    		features[action][Q4_CLOSE] = 1;
    	}
    	if(enemyScene[y][x - 1] != 0){
    		features[action][Q5_CLOSE] = 1;
    	}
    	if(enemyScene[y - 1][x - 1] != 0){
    		features[action][Q6_CLOSE] = 1;
    	}
    	if(enemyScene[y - 1][x] != 0){
    		features[action][Q7_CLOSE] = 1;
    	}
    	if(enemyScene[y - 1][x + 1] != 0){
    		features[action][Q8_CLOSE] = 1;
    	}
    }

    public static void setEnemyFar(byte[][] enemyScene, double[][] features, int action){
    	int x = MARIO_LOCATION.x;
    	int y = MARIO_LOCATION.y;
    	if(enemyScene[y - 1][x + 3] != 0 || enemyScene[y - 1][x + 4] != 0){
    		features[action][Q1_FAR] = 1;
    	}
    	else if(enemyScene[y][x + 3] != 0 || enemyScene[y][x + 4] != 0){
    		features[action][Q2_FAR] = 1;
    	}
    	else if(enemyScene[y + 1][x + 3] != 0 || enemyScene[y + 1][x + 4] != 0){
    		features[action][Q3_FAR] = 1;
    	}
    }



    public static void setEnemyFarther(byte[][] enemyScene, double[][] features, int action){
    	int x = MARIO_LOCATION.x;
    	int y = MARIO_LOCATION.y;
    	if(enemyScene[y - 1][x + 5] != 0 || enemyScene[y - 1][x + 6] != 0){
    		features[action][Q1_FAR] = 1;
    	}
    	else if(enemyScene[y][x + 5] != 0 || enemyScene[y][x + 6] != 0){
    		features[action][Q2_FAR] = 1;
    	}
    	else if(enemyScene[y + 1][x + 5] != 0 || enemyScene[y + 1][x + 6] != 0){
    		features[action][Q3_FAR] = 1;
    	}
    }

    public static void setEnemyMed(byte[][] enemyScene, double[][] features, int action){
    	int marioX = MARIO_LOCATION.x;
    	int marioY = MARIO_LOCATION.y;
    	//System.out.println("MEEp");
    	for(int y = marioY - 2; y <= marioY + 2; y += 4){
	    	for(int x = marioX - 2; x <= marioX + 2; x++){
	    		//System.out.println("x: " + x + " y: " + y);
	    		if(y == marioY - 2){
	    			if (x >= marioX - 2 && x <= marioX - 1 && enemyScene[y][x] != 0){
	    				features[action][Q4_MED] = 1;
	    			} 
	    			else if (x == marioX && enemyScene[y][x] != 0){
	    				features[action][Q5_MED] = 1;
	    			} 
	    			else if(x > marioX && x <= marioX + 2 && enemyScene[y][x] != 0){
	    				features[action][Q2_MED] = 1;
	    			}
	    		} else{
	    			if (x >= marioX - 2 && x <= marioX - 1 && enemyScene[y][x] != 0){
	    				features[action][Q6_MED] = 1;
	    			} 
	    			else if (x == marioX && enemyScene[y][x] != 0){
	    				features[action][Q7_MED] = 1;
	    			} 
	    			else if(x > marioX && x <= marioX + 2 && enemyScene[y][x] != 0){
	    				features[action][Q8_MED] = 1;
	    			}
	    		}
	    	}
	    }
	    //System.out.println("MOOp");
	    for(int x = marioX - 2; x <= marioX + 2; x += 4){
	    	for(int y = marioY - 1; y <= marioY + 1; y ++){
	    		if(x == marioX - 2){
	    			if(y == marioY - 1 && enemyScene[y][x] != 0 ){
	    				features[action][Q4_MED] = 1;
	    			}
	    			else if(y == marioY && enemyScene[y][x] != 0 ){
	    				features[action][Q5_MED] = 1;
	    			}
	    			else if(y == marioY + 1 && enemyScene[y][x] != 0 ){
	    				features[action][Q6_MED] = 1;
	    			}
	    		}
	    		else{
	    			if(y == marioY - 1 && enemyScene[y][x] != 0 ){
	    				features[action][Q2_MED] = 1;
	    			}
	    			else if(y == marioY && enemyScene[y][x] != 0 ){
	    				features[action][Q1_MED] = 1;
	    			}
	    			else if(y == marioY + 1 && enemyScene[y][x] != 0 ){
	    				features[action][Q8_MED] = 1;
	    			}
	    		}
	    	}
	    }
    }



	public static double[][] extractFeatures(Environment observation, int action) {
		/*System.out.println("__________________");
		print2dLevel(observation.getLevelSceneObservation());
		System.out.println("__________________");*/
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
		//features[action][ON_GROUND] = observation.isMarioOnGround() ? 1 : 0;
		//features[action][CAN_JUMP] = observation.mayMarioJump() ? 1 : 0;
		byte [][] levelScene = observation.getLevelSceneObservation();
		features[action][DANGER_OF_GAP] = DangerOfGap(levelScene) ? 1 : 0;

		//Ledge Features
		boolean facingLedge = (distanceToLedge(levelScene) != -1);
		/*DONT CARE*/
		//features[action][MARIO_FACING_LEDGE] = 0;
		features[action][SMALL_LEDGE] = LedgeRange(levelScene) == SMALL_LEDGE_RANGE ? 1: 0;
		features[action][MEDIUM_LEDGE] = LedgeRange(levelScene) == MEDIUM_LEDGE_RANGE ? 1: 0;
		features[action][LARGE_LEDGE] = LedgeRange(levelScene) == LARGE_LEDGE_RANGE ? 1: 0;
		features[action][LARGER_LEDGE] = LedgeRange(levelScene) > LARGE_LEDGE_RANGE ? 1: 0;
		float currXPos = observation.getMarioFloatPos()[0];
		features[action][MARIO_IS_STUCK] = MarioIsStuck(currXPos, facingLedge) ? 1: 0;

		//Ledge distance
		int distToLedge = distanceToLedge(levelScene);
		features[action][MARIO_IN_FRONT_LEDGE] = distToLedge == 1 ? 1:0;
		/*DONT CARE*/
		features[action][MARIO_CLOSE_TO_LEDGE] = (distToLedge >= 2 && distToLedge <= 4) ? 1:0; 
		/*DONT CARE*/
		features[action][MARIO_NOT_CLOSE_TO_LEDGE] = distToLedge == -1 ? 1:0;

		//Jumped facing ledge
		features[action][IN_FRONT_OF_LEDGE_JUMPED] = inFrontOfLedgeJumped(observation, facingLedge, levelScene) ? 1:0;

		//In air facing ledge	
		
		//features[action][AIR_FACING_LEDGE] = (facingLedge && !observation.isMarioOnGround()) ? 1:0;
		//System.out.println("FACING LEDGE JUMPED");

		//Enemy proximity features
		byte [][] enemyScene = observation.getEnemiesObservation(); 
		setEnemyClose(enemyScene, features, action);

		setEnemyMed(enemyScene, features, action);

		setEnemyFar(enemyScene, features, action);

		setEnemyFarther(enemyScene, features, action);

		features[action][GAP_CLOSE] = gapExists(levelScene, 12, 14) ? 1:0;
		features[action][GAP_MED] = gapExists(levelScene, 15, 17) ? 1:0;
		features[action][GAP_FAR] = gapExists(levelScene, 17, 19) ? 1:0;

		//System.out.print("Printing Level\n");
		//print2dArray(features);
		//System.out.print("\n\n\n\n");
		//print2dLevel(levelScene);
		//System.out.print("\n\n\n\n");

		return features;
	}

}

