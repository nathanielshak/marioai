package ch.idsia.ai.agents;
import ch.idsia.mario.environments.Environment;
import java.awt.Point;


public class FeatureExtractor {

	private static final Point MARIO_LOCATION = new Point(11,11);
	private static final int LEDGE = -10;
	private static final int FLOATING_LEDGE = -11;
	private static final int PIPE = 2;
	private static final int NUM_ACTIONS = 5;
	private static final int NUM_FEATURES = 4;
	private static final int ON_GROUND = 0;
	private static final int CAN_JUMP = 1;
	private static final int DANGER_OF_GAP = 2;
	private static final int MARIO_FACING_LEDGE = 3;
	// 0 = mario on ground
	// 1 = mario can jump

	 private static void print2dArray(byte[][] arr){
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
    private static boolean FacingLedge(byte[][] levelScene){

    	for(int x = MARIO_LOCATION.x + 1; x < levelScene.length; x++)
    	{
    		if(levelScene[x][MARIO_LOCATION.y] == LEDGE)
    		{
    			return true;
    		}
    	}

    	return false;

    }


	public static float[][] extractFeatures(Environment observation, int action) {
		float[][] features = new float[NUM_ACTIONS][NUM_FEATURES];
		for(int curAction = 0; curAction < NUM_ACTIONS; curAction++){
			if (action != curAction){
				for(int curFeature = 0; curFeature < NUM_FEATURES; curFeature++){
					features[curAction][curFeature] = 0;
				}
			}
		}
		//populate features of taken action column
		features[action][ON_GROUND] = observation.isMarioOnGround() ? 1 : 0;
		features[action][CAN_JUMP] = observation.mayMarioJump() ? 1 : 0;
		byte [][] levelScene = observation.getLevelSceneObservation();
		features[action][DANGER_OF_GAP] = DangerOfGap(levelScene) ? 1 : 0;
		features[action][MARIO_FACING_LEDGE] = FacingLedge(levelScene) ? 1 : 0;
		print2dArray(levelScene);

		return features;
	}



}