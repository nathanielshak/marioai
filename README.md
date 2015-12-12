# marioai
CS221 Project
Mario AI library downloaded from: http://www.marioai.org/

To compile the program, you must run: ant compile from the root directory.
In order to test our program, you can run: java ch.idsia.scenarios.Play ch.idsia.ai.agents.ai.HelloWorld {iterations} {leveldifficulty}, 
where the level iterations is how long you want to test mario and leveldifficulty is an integer representing how hard you want the level to be. You must run this from marioai/classes.

Within mario/src/ch/idsia/ai/agents/ai/HelloWorld.java, if the training boolean is set to true, then it will run the programs for the number of iterations specified and output the updated weight vector at each iteration. If not, it will test with the weights that the variable testWeights is set too. When the program is training, the weights will be outputed at the end of the program.

Within marioai/src/ch/idsia/scenarios/Play.java, if observe is set to true, then the game visualization is shown. If maxSpeed is set to true, then the game runs as fast as possible.

   
