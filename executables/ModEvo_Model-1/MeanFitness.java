/*
	Name: Elizabeth Brooks
	File: MeanFitness
	Modified: May 05, 2016
*/

//Imports
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//A class to calculate the mean fitness of a species through simulation
public class MeanFitness extends IndividualFitness{
	
	//Class fields for calculating mean fitness
    private SpeciesCharacteristics speciesValues;
    //private GaussianDistribution gaussianDistObject;
	private double meanTraitOne;
	private double meanTraitTwo;
	private double traitOne;
	private double traitTwo;
	private double varianceTraitOne;
	private double varianceTraitTwo;
	private double standardDevianceTraitOne;
	private double standardDevianceTraitTwo;
	private double meanFitness;
	private double[] individualFitnessArray;
	//Fields for creating and storing the values for simulation of normally distributed populations
	private Random randomSimulation;
	private int numIterations; //The number of iterations the user would like the models to be run for
	private int simPopSize; //The number of generations to be simulated for calc of mean fitness
	private double[] traitTwoValuesArray;
	private double[] traitOneValuesArray;
    //private int fileNum; //Current test file number
 	private String distributionChoice; //The name of the distribution to be used for simulations
	
	//The class constructor
	public MeanFitness(SpeciesCharacteristics speciesInputs, double nextGenMeanTraitOneInput, double nextGenMeanTraitTwoInput)
	{
    	//Call IndividualFitness (super) constructor
      	super(speciesInputs);
		//Initialize reference variables
		speciesValues = speciesInputs;
      	//gaussianDistObject = new GaussianDistribution();
		//Set initial values
      	//fileNum = 1;
		numIterations = speciesValues.getNumIterations();
		simPopSize = speciesValues.getSimPopSize();
		meanTraitOne = nextGenMeanTraitOneInput;
		meanTraitTwo = nextGenMeanTraitTwoInput;
		varianceTraitOne = speciesValues.getPhenotypicVarianceTraitOne();
		varianceTraitTwo = speciesValues.getPhenotypicVarianceTraitTwo();
		calculateStandardDeviationTraitOne();
		calculateStandardDeviationTraitTwo();
	}
	
	//A method to calculate the standard deviation of trait two
	public void calculateStandardDeviationTraitTwo()
	{
		standardDevianceTraitTwo = Math.sqrt(Math.abs(varianceTraitTwo));
	}
	
	//A method to calculate the standard deviation of trait one
	public void calculateStandardDeviationTraitOne()
	{
		standardDevianceTraitOne = Math.sqrt(Math.abs(varianceTraitOne));
	}
	
	//A method to simulate as many populations for trait one as specified
	public void calcSimulatedTraitOneValues(double meanTraitOneInput)
	{
		traitOneValuesArray = new double[simPopSize];
     	meanTraitOne = meanTraitOneInput;
      	//Depending on user selection for trait distribution, determine the current mean fitness of the population
		//Default distribution is the provided .nextGaussian function
		if(speciesValues.getDistributionName().equals("defaultdistribution")){
			randomSimulation = new Random();
			//A for loop to fill the array with a random number for trait one
			//through the simulation of normally distributed populations
			for(int i=0; i<traitOneValuesArray.length; i++){
				traitOne = (randomSimulation.nextGaussian() * standardDevianceTraitOne + meanTraitOne);
	         //Make sure that the trait values do not fall below zero
	         if(traitOne < 0){
	            traitOne = 0;
	         } 
				traitOneValuesArray[i] = traitOne;
			}
		}else if(speciesValues.getDistributionName().equals("gaussiandistribution")){
			//gaussianDistObject.getGaussianDistribution();
			System.out.println("Distribution not yet ready for use, program exited");
			System.exit(0);
		}
	}
	
	//A method to simulate as many populations for trait two as specified
	public void calcSimulatedTraitTwoValues(double meanTraitTwoInput)
	{
		traitTwoValuesArray = new double[simPopSize];
        meanTraitTwo = meanTraitTwoInput;
      	//Depending on user selection for trait distribution, determine the current mean fitness of the population
		//Default distribution is the provided .nextGaussian function
		if(speciesValues.getDistributionName().equals("defaultdistribution")){
			randomSimulation = new Random();
			//A for loop to fill the array with a random number for trait one
			//through the simulation of normally distributed populations
			for(int i=0; i<traitTwoValuesArray.length; i++){
				traitTwo = (randomSimulation.nextGaussian() * standardDevianceTraitTwo + meanTraitTwo);
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
	
	//A method to calculate the mean fitness
	public void calculateMeanFitness(double meanTraitOneInput, double meanTraitTwoInput)
	{
		//Initialize fields with inputs
		meanTraitOne = meanTraitOneInput;
		meanTraitTwo = meanTraitTwoInput;
		//Array for storing the calculated individual fitness values
		individualFitnessArray = new double[simPopSize];
		//Calculate individual trait values
		calcSimulatedTraitOneValues(meanTraitOne);
		calcSimulatedTraitTwoValues(meanTraitTwo);

		//Create a file for the first, 10th, and 100th, simulated populations
	    /*if(fileNum == 1){
	   		writeTestFileOne();
	        writeTestFileTwo();
	    }else if(fileNum == 10){
	        writeTestFileOne();
	        writeTestFileTwo();
	    }else if(fileNum == 100){
	        writeTestFileOne();
	        writeTestFileTwo();
	    }*/
      
		//A for loop to fill the array with the individualFitnessValues
		for(int i=0; i<individualFitnessArray.length; i++){
			individualFitnessArray[i] = super.getIndividualFitness(traitOneValuesArray[i], traitTwoValuesArray[i]);
		}
		
		//A for loop to calculate the mean fitness
		for(int i=0; i<individualFitnessArray.length; i++){
			meanFitness += individualFitnessArray[i];
		}
		meanFitness /= simPopSize;
      
      	//Incriment the file number
      	//fileNum++;
	}
	
	//Methods to return mean values
	public double getMeanFitnessSim(double meanTraitOneInput, double meanTraitTwoInput)
	{
		//Set initial values
		meanTraitOne = meanTraitOneInput;
		meanTraitTwo = meanTraitTwoInput;
		
      	//Calc mean fitness
		calculateMeanFitness(meanTraitOneInput, meanTraitTwoInput);
      
      	//Return the calculated mean fitness
		return meanFitness;
	}
	
	//Getter methods
   public double getMeanTraitOne() {
		return meanTraitOne;
	}
   
   public double getMeanTraitTwo() {
		return meanTraitTwo;
	}
   
   public double getVarianceTraitOne() {
		return varianceTraitOne;
	}
   
   public double getVarianceTraitTwo() {
		return varianceTraitTwo;
	}
   
   public double getStandardDevianceTraitOne() {
		return standardDevianceTraitOne;
	}
   
   public double getStandardDevianceTraitTwo() {
		return standardDevianceTraitTwo;
	}
   
   public double getMeanFitness() {
		return meanFitness;
	}
   
	public int getSimPopSize() {
		return simPopSize;
	}
	
	public int getNumIterations() {
		return numIterations;
	}
   
   /*public int getFileNum() {
		return fileNum;
	}*/
   
   //Setter methods
   public void setMeanTraitOne(double meanTraitOneInput) {
		meanTraitOne = meanTraitOneInput;
	}
   
   public void setMeanTraitTwo(double meanTraitTwoInput) {
		meanTraitTwo = meanTraitTwoInput;
	}
   
   public void setVarianceTraitOne(double varianceTraitOneInput) {
		varianceTraitOne = varianceTraitOneInput;
	}
   
   public void setVarianceTraitTwo(double varianceTraitTwoInput) {
		varianceTraitTwo = varianceTraitTwoInput;
	}
   
   public void setStandardDevianceTraitOne(double standardDevianceTraitOneInput) {
		standardDevianceTraitOne = standardDevianceTraitOneInput;
	}
   
   public void setStandardDevianceTraitTwo(double standardDevianceTraitTwoInput) {
		standardDevianceTraitTwo = standardDevianceTraitTwoInput;
	}
   
   public void setMeanFitness(double meanFitnessInput) {
		meanFitness = meanFitnessInput;
	}
   
	public void setSimPopSize(int simPopSizeInput) {
		simPopSize = simPopSizeInput;
	}
	
	public void setNumIterations(int numIterationsInput) {
		numIterations = numIterationsInput;
	}
   
   /*public void setFileNum(int fileNumInput) {
		fileNum = fileNumInput;
	}*/
}