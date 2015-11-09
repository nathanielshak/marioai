package ch.idsia.ai.agents;
import ch.idsia.mario.environments.Environment;


public class FeatureExtractor {

	private static final int NUM_ACTIONS = 5;
	private static final int NUM_FEATURES = 2;
	private static final int ON_GROUND = 0;
	private static final int CAN_JUMP = 1;
	// 0 = mario on ground
	// 1 = mario can jump




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

		return features;
	}



}