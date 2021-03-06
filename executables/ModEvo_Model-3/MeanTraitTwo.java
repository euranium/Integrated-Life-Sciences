/*
	Name: Elizabeth Brooks
	File: MeanTraitTwo
	Modified: October 30, 2016
*/

//Imports
import java.security.SecureRandom;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//A class to calculate the mean fitness of a species through simulation
public class MeanTraitTwo {
	
	//Class fields used to calculate the mean value of trait Two
	private SecureRandom randomSimulation; //For simulation of individual variable values
   private SpeciesCharacteristics speciesValues; //Reference variable of the SpeciesCharacteristics class
   private IndividualTraitTwo individualTraitTwoObject; //Reference variable of the IndividualTraitTwo class
   //private GaussianDistribution gaussianDistObject; //Reference variable of the GaussianDistribution class
	private int numIterations; //The number of generations to be calculated
	private int simPopSize; //The number of generations to be simulated for calc of mean fitness
	//Variables for determining mean trait Two through simulation
	private double varianceTraitTwo;
	private double standardDevianceTraitTwo;
	private double[] traitTwoValuesArray;

	//The class constructor
	public MeanTraitTwo(SpeciesCharacteristics speciesInputs)
	{
      //Initialize reference variables
      speciesValues = speciesInputs;
      individualTraitTwoObject = new IndividualTraitTwo(speciesValues);
		//Initialize class fields
		speciesValues = speciesInputs;
		numIterations = speciesValues.getNumIterations();
		simPopSize = speciesValues.getSimPopSize();
		varianceTraitTwo = speciesValues.getVarianceTraitTwo();
		calculateStandardDeviationTraitTwo();
	}
	
	//A method to calculate the standard deviation of trait Two
	public void calculateStandardDeviationTraitTwo()
	{
		standardDevianceTraitTwo = Math.sqrt(Math.abs(varianceTraitTwo));
	}
	
	//A method to simulate as many populations for trait Two as specified
	public void calcSimulatedTraitTwoValues(double meanSlopeReactionNormInput, double meanInterceptReactionNormInput, double meanFunctionTraitInput)
	{
      //Set initial values
      double traitTwo;
      double meanSlopeReactionNorm = meanSlopeReactionNormInput;
      double meanInterceptReactionNorm = meanInterceptReactionNormInput;
      double meanFunctionTrait = meanFunctionTraitInput;
		traitTwoValuesArray = new double[simPopSize];
      double individualTraitTwo = individualTraitTwoObject.getIndividualTraitTwo(meanSlopeReactionNorm, meanInterceptReactionNorm, meanFunctionTrait);
      if(speciesValues.getDistributionName().equals("defaultdistribution")){
			randomSimulation = new SecureRandom();
			//A for loop to fill the array with a random number for trait Two
			//through the simulation of normally distributed populations
			for(int i=0; i<traitTwoValuesArray.length; i++){
				//Send the appropriate values to the IndividualTraitTwo class to recalculate a new mean for trait Two
				traitTwo = (randomSimulation.nextGaussian() * standardDevianceTraitTwo + individualTraitTwo);
	         //Make sure that the trait values do not fall below zero
	         if(traitTwo < 0){
	           	traitTwo = 0;
	         }
	         traitTwoValuesArray[i] = traitTwo;
			}
		}else if(speciesValues.getDistributionName().equals("gaussiandistribution")){
			//gaussianDistObject.getGaussianDistribution();
			System.out.println("Distribution not yet ready for use, program exited");
			System.exit(0);
		}
	}
	
	//Methods to return mean values
	public double getMeanTraitTwoSim(double nextGenMeanSlopeReactionNormInput, double nextGenMeanInterceptReactionNormInput, 
      double nextGenMeanFunctionTraitInput)
	{
      //Set the initial values
      double meanTraitTwo = 0;
      double meanFunctionTrait = nextGenMeanFunctionTraitInput;
		double meanInterceptReactionNorm = nextGenMeanInterceptReactionNormInput;
		double meanSlopeReactionNorm = nextGenMeanSlopeReactionNormInput;
		//Depending on user selection for trait distribution, determine the current mean fitness of the population
		//Default distribution is the provided .nextGaussian function
      calcSimulatedTraitTwoValues(meanSlopeReactionNorm, meanInterceptReactionNorm, meanFunctionTrait);
		//A for loop to calculate the mean fitness
		for(int i=0; i<traitTwoValuesArray.length; i++){
			meanTraitTwo += traitTwoValuesArray[i];
		}
		meanTraitTwo /= simPopSize;

      	return meanTraitTwo;
	}

	//Getter methods
   public double getVarianceTraitTwoSim() {
		return varianceTraitTwo;
	}
   
   public double getStandardDevianceTraitTwoSim() {
		return standardDevianceTraitTwo;
	}
   
	public int getSimPopSizeSim() {
		return simPopSize;
	}
	
	public int getNumIterationsSim() {
		return numIterations;
	}
   
   //Setter methods   
   public void setVarianceTraitTwoSim(double varianceTraitTwoInput) {
		varianceTraitTwo = varianceTraitTwoInput;
	}
   public void setStandardDevianceTraitTwoSim(double standardDevianceTraitTwoInput) {
		standardDevianceTraitTwo = standardDevianceTraitTwoInput;
	}
   
	public void setSimPopSizeSim(int simPopSizeInput) {
		simPopSize = simPopSizeInput;
	}
	
	public void setNumIterationsSim(int numIterationsInput) {
		numIterations = numIterationsInput;
	}
}