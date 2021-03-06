/*
	Name: Elizabeth Brooks
	File: ModelThree
	Modified: October 29, 2016
*/

//Imports
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//Class for modeling the evolutionary trajectories of a species with respect to multiple phenotypic traits
//while also accounting for underlying developmental interactions
public class ModelThree {

   //Class fields to store variables
   private SpeciesCharacteristics speciesValues; //Reference variable of the SpeciesCharacteristics class
   private IndividualFitness individualFitnessObject; //Reference variable of the IndividualFitness class
	private MeanFitness meanFitnessObject; //Reference variable of the MeanFitness class
	private MeanTraitOne meanTraitOneObject; //Reference variable of the MeanTraitOne class
	private MeanTraitTwo meanTraitTwoObject; //Reference variable of the MeanTraitTwo class
	private IndividualTraitOne individualTraitOneObject; //Reference variable of the IndividualTraitOne class
	private IndividualTraitTwo individualTraitTwoObject; //Reference variable of the IndividualTraitTwo class
	private int numIterations; //The number of iterations the user would like the models to be run for
	private int simPopSize; //The number of generations to be simulated for calc of mean fitness
   private double phenotypicVarianceTraitOne; //The phenotypic variance of trait two
	private double phenotypicVarianceTraitTwo; //The phenotypic variance of trait two
	private double phenotypicVarianceInterceptReactionNorm; //The phenotypic variance of the intercept of the reaction norm
	private double phenotypicVarianceSlopeReactionNorm; //The phenotypic variance of the slope of the reaction norm
   private double phenotypicVarianceFunctionTrait; //The phenotypic variance of the phenotypic trait acting as a function of traits one and two
	private double[][] heritabilityMatrix; //Matrix of heritability values
	private double meanFitness; //Mean fitness
	//Fields to hold the values for calculating each generation's values
	private double nextGenMeanTraitOne; //The mean of trait one
	private double nextGenMeanTraitTwo; //The mean of trait two
	private double nextGenMeanInterceptReactionNorm; //The next generation's mean intercept of the reaction norm
	private double nextGenMeanSlopeReactionNorm; //The next gerneration's mean value for the slope of the reaction norm
   private double nextGenMeanFunctionTrait; //The next generation's mean value for the function of traits one and two
  	//Arrays to hold each generations calculated mean trait value
   private double[] fitnessArray;
	private double[] traitOneArray; //Array of mean values for the first trait
	private double[] traitTwoArray; //Array of mean values for the second trait
	private double[] slopeArray; //Array of mean slope values of the reaction norm
	private double[] interceptArray; //Array of mean intercept values of the reaction norm
	private double[] preferenceArray; //Array of mean preference values
   //Variables for calculating vectors
   private double traitTwoInterceptDerivative;
	private double traitOneInterceptDerivative;
   private double traitOneSlopeDerivative;
	private double traitTwoSlopeDerivative;
   private double traitOneFunctionTraitDerivative;
	private double traitTwoFunctionTraitDerivative;
   private double individualFitnessTraitOneDerivative;
   private double individualFitnessTraitTwoDerivative;
	private double fitnessInverse;
	private double nextGenVectorPositionOne;
   private double nextGenVectorPositionTwo;
   private double nextGenVectorPositionThree;
   private double[][] nextGenHeritabilityMatrix;
   
	//The class constructor
	public ModelThree(SpeciesCharacteristics speciesInputs)
	{
		//Initialize species characteristics
      speciesValues = speciesInputs;
		//Set initial values
		numIterations = speciesValues.getNumIterations();
		simPopSize = speciesValues.getSimPopSize();
		phenotypicVarianceSlopeReactionNorm = speciesValues.getPhenotypicVarianceSlopeReactionNorm();
		nextGenMeanTraitOne = speciesValues.getMeanTraitOne();
		nextGenMeanTraitTwo = speciesValues.getMeanTraitTwo();
		phenotypicVarianceTraitTwo = speciesValues.getPhenotypicVarianceTraitTwo();
		nextGenMeanInterceptReactionNorm = speciesValues.getMeanInterceptReactionNorm();
		nextGenMeanSlopeReactionNorm = speciesValues.getMeanSlopeReactionNorm();
      nextGenMeanFunctionTrait = speciesValues.getMeanFunctionTrait();
	   phenotypicVarianceInterceptReactionNorm = speciesValues.getPhenotypicVarianceInterceptReactionNorm();
	   phenotypicVarianceSlopeReactionNorm = speciesValues.getPhenotypicVarianceSlopeReactionNorm();
	}
	
	//Methods to run the model
	public void runModel()
	{
		//Initialize objects
		individualFitnessObject = new IndividualFitness(speciesValues);		
		individualTraitOneObject = new IndividualTraitOne(speciesValues);
		individualTraitTwoObject = new IndividualTraitTwo(speciesValues);
		meanFitnessObject = new MeanFitness(speciesValues);
		meanTraitOneObject = new MeanTraitOne(speciesValues);
		meanTraitTwoObject = new MeanTraitTwo(speciesValues);
		//Begin the model looping
		setTraitArrays();
	}

	//Add each generations mean trait values into the trait arrays
	public void setTraitArrays()
	{
		fitnessArray = new double [numIterations];
		traitOneArray = new double[numIterations];
		traitTwoArray = new double[numIterations];
		slopeArray = new double[numIterations];
		interceptArray = new double[numIterations];
		preferenceArray = new double[numIterations];
		//A for loop to save each value calculated for the mean of trait one and two
		for(int i=0;i<numIterations;i++){
         fitnessArray[i] = meanFitness;
			traitOneArray[i] = nextGenMeanTraitOne;
			traitTwoArray[i] = nextGenMeanTraitTwo;
			slopeArray[i] = nextGenMeanSlopeReactionNorm;
			interceptArray[i] = nextGenMeanInterceptReactionNorm;
			preferenceArray[i] = nextGenMeanFunctionTrait;
			calcNextGenTraitOneTraitTwo();
			calcNextGenInterceptSlopeFunctionTrait();
		}
      	//Write to CSV file trait values
         writeFitnessFile();
      	writeTraitTwoFile();
      	writeTraitOneFile();
      	writeSlopeFile();
      	writeInterceptFile();
      	writePreferenceFile();
   }
	
	//Send the new mean values of each trait to the IndividualFitness class for re calculation
	public void calcNextGenTraitOneTraitTwo()
	{
		nextGenMeanTraitOne = meanTraitOneObject.getMeanTraitOneSim(nextGenMeanSlopeReactionNorm, nextGenMeanInterceptReactionNorm, 
         nextGenMeanFunctionTrait);
		nextGenMeanTraitTwo = meanTraitTwoObject.getMeanTraitTwoSim(nextGenMeanSlopeReactionNorm, nextGenMeanInterceptReactionNorm, 
         nextGenMeanFunctionTrait);
	}
   
   //A method to determine which value to return from the heritability matrix
	public void calcNextGenInterceptSlopeFunctionTrait()
	{
      //Initialize constants
      calcConstants();
      //Multiply heritability matrix by the vector
      calcFinalVectorValues();
      //Calc next generation intercept, slope, and functuon trait values
		nextGenMeanInterceptReactionNorm += nextGenVectorPositionOne;
		nextGenMeanSlopeReactionNorm += nextGenVectorPositionTwo;
		nextGenMeanFunctionTrait += nextGenVectorPositionThree;
	}
   
   //A method to determine which value to return from the heritability matrix
	public void calcConstants()
	{
      //Initialize values
      meanFitness = meanFitnessObject.getMeanFitnessSim(nextGenMeanTraitOne, nextGenMeanTraitTwo);
		traitTwoInterceptDerivative = individualTraitTwoObject.numericallyCalcInterceptPartialDerivative(nextGenMeanSlopeReactionNorm, 
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
		traitOneInterceptDerivative = individualTraitOneObject.numericallyCalcInterceptPartialDerivative(nextGenMeanSlopeReactionNorm, 
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
      traitOneSlopeDerivative = individualTraitOneObject.numericallyCalcSlopePartialDerivative(nextGenMeanSlopeReactionNorm, 
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
		traitTwoSlopeDerivative = individualTraitTwoObject.numericallyCalcSlopePartialDerivative(nextGenMeanSlopeReactionNorm,
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
      traitOneFunctionTraitDerivative = individualTraitOneObject.numericallyCalcFunctionTraitPartialDerivative(nextGenMeanSlopeReactionNorm, 
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
		traitTwoFunctionTraitDerivative = individualTraitTwoObject.numericallyCalcFunctionTraitPartialDerivative(nextGenMeanSlopeReactionNorm, 
				nextGenMeanInterceptReactionNorm, nextGenMeanFunctionTrait);
      individualFitnessTraitOneDerivative = individualFitnessObject.numericallyCalcTraitOnePartialDerivative(nextGenMeanTraitOne, nextGenMeanTraitTwo);
      individualFitnessTraitTwoDerivative = individualFitnessObject.numericallyCalcTraitTwoPartialDerivative(nextGenMeanTraitOne, nextGenMeanTraitTwo);
		fitnessInverse = (1/meanFitness);
	}
   
   //A method to calculate the mean intercept of the reaction norm 
	public void calcFinalVectorValues()
	{
      //Calc first vector position values
		double vectorOnePositionOne = (fitnessInverse * individualFitnessTraitTwoDerivative * traitTwoInterceptDerivative);
      double vectorOnePositionTwo = (fitnessInverse * individualFitnessTraitTwoDerivative * traitTwoSlopeDerivative);
      double vectorOnePositionThree = (fitnessInverse * individualFitnessTraitTwoDerivative * traitTwoFunctionTraitDerivative);
      //Calc second vector position values
		double vectorTwoPositionOne = (fitnessInverse * individualFitnessTraitOneDerivative * traitOneInterceptDerivative);
      double vectorTwoPositionTwo = (fitnessInverse * individualFitnessTraitOneDerivative * traitOneSlopeDerivative);
      double vectorTwoPositionThree = (fitnessInverse * individualFitnessTraitOneDerivative * traitOneFunctionTraitDerivative);
      //Calc final vector position values
      double vectorPositionOne = vectorOnePositionOne + vectorTwoPositionOne;
      double vectorPositionTwo = vectorOnePositionTwo + vectorTwoPositionTwo;
      double vectorPositionThree = vectorOnePositionThree + vectorTwoPositionThree;
      //Send vector values to be multiplied by heritability matrix values
      multiplyMatrixByVector(vectorPositionOne, vectorPositionTwo, vectorPositionThree);      
	}
   
   //A method to calculate the mean intercept of the reaction norm 
	public void multiplyMatrixByVector(double vectorPositionOneInput, double vectorPositionTwoInput, double vectorPositionThreeInput)
	{
      //Initialize vector position values
		double vectorPositionOne = vectorPositionOneInput;
      double vectorPositionTwo = vectorPositionTwoInput;
      double vectorPositionThree = vectorPositionThreeInput;
		//Initialize the 2D array with 3 rows and 3 columns and set the matrix values
		//Copy the matrix from the SpecieCharacteristics class
		/*int tempLength = speciesValues.getHeritabilityMatrix().length;
		heritabilityMatrix = new double[tempLength][];
		for(int m=0; m<tempLength; m++)
		{
		  double[] tempMatrix = speciesValues.getHeritabilityMatrix()[m];
		  tempLength = speciesValues.getHeritabilityMatrix().length;
		  heritabilityMatrix[m] = new double[tempLength];
		  System.arraycopy(tempMatrix, 0, heritabilityMatrix[m], 0, tempLength);
		}*/
      heritabilityMatrix = new double[][]{
         {0.5, 0, 0},
	      {0, 0.5, 0},
	      {0, 0, 0.5}
      };
		//Calc matrix values
		double RowOneColumnOne = (vectorPositionOne * heritabilityMatrix[0][0]);
      double RowOneColumnTwo = (vectorPositionTwo * heritabilityMatrix[0][1]);
		double RowOneColumnThree = (vectorPositionThree * heritabilityMatrix[0][2]);
		double RowTwoColumnOne = (vectorPositionOne * heritabilityMatrix[1][0]); 
		double RowTwoColumnTwo = (vectorPositionTwo * heritabilityMatrix[1][1]);
		double RowTwoColumnThree = (vectorPositionThree * heritabilityMatrix[1][2]);
		double RowThreeColumnOne = (vectorPositionOne * heritabilityMatrix[2][0]);
		double RowThreeColumnTwo = (vectorPositionTwo * heritabilityMatrix[2][1]);
		double RowThreeColumnThree = (vectorPositionThree * heritabilityMatrix[2][2]);
      //Calc final vector position values
		nextGenVectorPositionOne = (RowOneColumnOne + RowOneColumnTwo + RowOneColumnThree);
      nextGenVectorPositionTwo = (RowTwoColumnOne + RowTwoColumnTwo + RowTwoColumnThree);
      nextGenVectorPositionThree = (RowThreeColumnOne + RowThreeColumnTwo + RowThreeColumnThree);
	}
   
   //Method to write trait one values to a TXT file
    public void writeFitnessFile()
    {
   	//Write to file the first traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         //int fileCount = 1;
         String meanFitnessPath = "meanFitnessValues_ModelThree.txt";
         File meanFitnessFile = new File(meanFitnessPath);         
         //Create meanTraitOneFile and file writer
         FileWriter fwFitness = new FileWriter(meanFitnessFile.getAbsoluteFile()); 
			meanFitnessFile.createNewFile();
            //Write to file the header
			fwFitness.write("Generation MeanFitness\n");
			String aOne;
			String bOne;			
			for(int i=0, k=1;i<fitnessArray.length;i++, k++){
				//Write to file generationNumber, traitOne
			   	aOne = Integer.toString(k);
			   	fwFitness.append(aOne);
			   	fwFitness.append(" ");
			   	bOne = Double.toString(fitnessArray[i]);
			   	fwFitness.append(bOne);
			   	fwFitness.append("\n");
			}			
			//Close the file
			fwFitness.close();
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
   }
      
	//Method to write trait one values to a TXT file
    public void writeTraitOneFile()
    {
   	//Write to file the first traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         //int fileCount = 1;
         String meanTraitOnePath = "meanTraitOneValues_ModelThree.txt";
         File meanTraitOneFile = new File(meanTraitOnePath);         
         /*if (meanTraitOneFile.exists()){
            //Loop through the existing files
			   while(meanTraitOneFile.exists()){
		   	   fileCount++;
               meanTraitOnePath = "meanTraitOneValues_GeneralModel.txt";
               meanTraitOneFile = new File(meanTraitOnePath);
		   	}*/
            //Create meanTraitOneFile and file writer
            FileWriter fwOne = new FileWriter(meanTraitOneFile.getAbsoluteFile()); 
				meanTraitOneFile.createNewFile();
            //Write to file the header
			   fwOne.write("Generation MeanTraitOne\n");
			   String aOne;
			   String bOne;			
			   for(int i=0, k=1;i<traitOneArray.length;i++, k++){
			   	//Write to file generationNumber, traitOne
			   	aOne = Integer.toString(k);
			   	fwOne.append(aOne);
			   	fwOne.append(" ");
			   	bOne = Double.toString(traitOneArray[i]);
			   	fwOne.append(bOne);
			   	fwOne.append("\n");
			   }			
			   //Close the file
			   fwOne.close();
         /*}else if(!meanTraitOneFile.exists()){
            //Create meanTraitOneFile and file writer
            FileWriter fwOne = new FileWriter(meanTraitOneFile.getAbsoluteFile()); 
				meanTraitOneFile.createNewFile();
            //Write to file the header
			   fwOne.write("Generation MeanTraitOne\n");
			   String aOne;
			   String bOne;			
			   for(int i=0, k=1;i<traitOneArray.length;i++, k++){
			   	//Write to file generationNumber, traitOne
			   	aOne = Integer.toString(k);
			   	fwOne.append(aOne);
			   	fwOne.append(" ");
			   	bOne = Double.toString(traitOneArray[i]);
			   	fwOne.append(bOne);
			   	fwOne.append("\n");
			   }			
			   //Close the file
			   fwOne.close();
			}else{
            //Display error message
            System.out.println("Error in file naming, GeneralModel");
            System.exit(0);
         }*/		
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
   }

   //Method to write trait one values to a TXT file
    public void writeTraitTwoFile()
    {
   	//Write to file the first traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         String meanTraitTwoPath = "meanTraitTwoValues_ModelThree.txt";
         File meanTraitTwoFile = new File(meanTraitTwoPath);         
            //Create meanTraitOneFile and file writer
            FileWriter fwTwo = new FileWriter(meanTraitTwoFile.getAbsoluteFile()); 
			meanTraitTwoFile.createNewFile();
            //Write to file the header
			fwTwo.write("Generation MeanTraitTwo\n");
			String aOne;
			String bOne;			
			for(int i=0, k=1;i<traitTwoArray.length;i++, k++){
				//Write to file generationNumber, traitOne
			   	aOne = Integer.toString(k);
			   	fwTwo.append(aOne);
			   	fwTwo.append(" ");
			   	bOne = Double.toString(traitTwoArray[i]);
			   	fwTwo.append(bOne);
			   	fwTwo.append("\n");
			}			
			//Close the file
			fwTwo.close();
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
   }
		
   //Method to write trait one values to a TXT file
   public void writeSlopeFile()
   {
		//Write to file the second traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         String meanSlopePath = "meanSlopeValues_ModelThree.txt";
         File meanSlopeFile = new File(meanSlopePath);
            //Create meanTraitOneFile and file writer
            FileWriter fwS = new FileWriter(meanSlopeFile.getAbsoluteFile()); 
			meanSlopeFile.createNewFile();
            //Write to file the header
			fwS.write("Generation MeanSlope\n");
			String aTwo;
			String bTwo;			
			for(int i=0, k=1;i<slopeArray.length;i++, k++){
			   	//Write to file generationNumber, traitOne
			   	aTwo = Integer.toString(k);
			   	fwS.append(aTwo);
			   	fwS.append(" ");
			   	bTwo = Double.toString(slopeArray[i]);
			   	fwS.append(bTwo);
			   	fwS.append("\n");	
			}			
			//Close the file
			fwS.close();
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
	}

	//Method to write trait one values to a TXT file
   public void writeInterceptFile()
   {
		//Write to file the second traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         String meanInterceptPath = "meanInterceptValues_ModelThree.txt";
         File meanInterceptFile = new File(meanInterceptPath);
            //Create meanTraitOneFile and file writer
            FileWriter fwI = new FileWriter(meanInterceptFile.getAbsoluteFile()); 
			meanInterceptFile.createNewFile();
            //Write to file the header
			fwI.write("Generation MeanIntercept\n");
			String aTwo;
			String bTwo;			
			for(int i=0, k=1;i<interceptArray.length;i++, k++){
			   	//Write to file generationNumber, traitOne
			   	aTwo = Integer.toString(k);
			   	fwI.append(aTwo);
			   	fwI.append(" ");
			   	bTwo = Double.toString(interceptArray[i]);
			   	fwI.append(bTwo);
			   	fwI.append("\n");	
			}			
			//Close the file
			fwI.close();
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
	}

	//Method to write trait one values to a TXT file
   public void writePreferenceFile()
   {
		//Write to file the second traits values
		//Catch exceptions and write to file in TXT format
		try {
         //Determine which test number is being run for file naming
         String meanCoefficientPath = "meanPreferenceValues_ModelThree.txt";
         File meanCoefficientFile = new File(meanCoefficientPath);
            //Create meanTraitOneFile and file writer
            FileWriter fwI = new FileWriter(meanCoefficientFile.getAbsoluteFile()); 
			meanCoefficientFile.createNewFile();
            //Write to file the header
			fwI.write("Generation MeanPreference\n");
			String aTwo;
			String bTwo;			
			for(int i=0, k=1;i<preferenceArray.length;i++, k++){
			   	//Write to file generationNumber, traitOne
			   	aTwo = Integer.toString(k);
			   	fwI.append(aTwo);
			   	fwI.append(" ");
			   	bTwo = Double.toString(preferenceArray[i]);
			   	fwI.append(bTwo);
			   	fwI.append("\n");	
			}			
			//Close the file
			fwI.close();
		} catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
		}
	}
	
   //Getter methods 
   public int getSimPopSizeInitial() 
   {
		return simPopSize;
	}
	
	public int getNumIterationsInitial() 
   {
		return numIterations;
	}	
	
   public double getMeanTraitOneCurrent()
	{
		return nextGenMeanTraitOne;
	}
	
	public double getMeanTraitTwoCurrent()
	{
		return nextGenMeanTraitTwo;
	}
	
	public double getMeanInterceptReactionNormCurrent() 
   {
		return nextGenMeanInterceptReactionNorm;
	}
	
	public double getMeanSlopeReactionNormCurrent() 
   {
		return nextGenMeanSlopeReactionNorm;
	}
   
   public double getMeanFunctionTraitCurrent() 
   {
		return nextGenMeanFunctionTrait;
	}
   
   public double getVarianceTraitOneInitial()
	{
		return phenotypicVarianceTraitOne;
	}
   
   public double getVarianceTraitTwoInitial()
	{
		return phenotypicVarianceTraitTwo;
	}
	
	public double getVarianceInterceptInitial() 
   {
		return phenotypicVarianceInterceptReactionNorm;
	}

	public double getVarianceSlopeInitial() 
   {
		return phenotypicVarianceSlopeReactionNorm;
	}
   
   public double getVarianceFunctionTraitInitial() 
   {
		return phenotypicVarianceFunctionTrait;
	}
   
   //Setter methods 
   public void setSimPopSizeInitial(int simPopSizeInput) 
   {
		simPopSize = simPopSizeInput;
	}
	
	public void setNumIterationsInitial(int numIterationsInput) 
   {
		numIterations = numIterationsInput;
	}	
	
	public void setMeanTraitOneCurrent(double nextGenMeanTraitOneInput)
	{
		nextGenMeanTraitOne = nextGenMeanTraitOneInput;
	}
	
	public void setMeanTraitTwoCurrent(double nextGenMeanTraitTwoInput)
	{
		nextGenMeanTraitTwo = nextGenMeanTraitTwoInput;
	}
	
	public void setMeanInterceptReactionNormCurrent(double nextGenMeanInterceptReactionNormInput) 
   {
		nextGenMeanInterceptReactionNorm = nextGenMeanInterceptReactionNormInput;
	}
	
	public void setMeanSlopeReactionNormCurrent(double nextGenMeanSlopeReactionNormInput) 
   {
		nextGenMeanSlopeReactionNorm = nextGenMeanSlopeReactionNormInput;
	}
   
   public void setMeanFunctionTraitCurrent(double nextGenMeanFunctionTraitInput) 
   {
		nextGenMeanFunctionTrait = nextGenMeanFunctionTraitInput;
	}
   	
	public void setVarianceInterceptInitial(double phenotypicVarianceInterceptReactionNormInput) 
   {
		phenotypicVarianceInterceptReactionNorm = phenotypicVarianceInterceptReactionNormInput;
	}

	public void setVarianceSlopeInitial(double phenotypicVarianceSlopeReactionNormInput) 
   {
		phenotypicVarianceSlopeReactionNorm = phenotypicVarianceSlopeReactionNormInput;
	}
   
   public void setVarianceFunctionTraitInitial(double phenotypicVarianceFunctionTraitInput) 
   {
		phenotypicVarianceFunctionTrait = phenotypicVarianceFunctionTraitInput;
	}
}
