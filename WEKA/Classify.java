
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;


public class Classify {
	
	public static void main(String[] args) throws Exception {
		// TODO - read csv with header.
		// TODO - classify not from arff but from the feature extracted (using attributes as in the header?)
		// TODO - check auc and accuracy.
		// TODO - integrate with NECTAR.
		
		// load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setNoHeaderRowPresent(true);
	    loader.setSource(new File("C:/EclipseWorkspace/DynamicFunctionChoose/src/MLFeatures.txt"));
	    Instances train = loader.getDataSet();
	 
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(train);
	    saver.setFile(new File("C:/EclipseWorkspace/DynamicFunctionChoose/src/MLFeatures1.arff"));
	    saver.setDestination(new File("C:/EclipseWorkspace/DynamicFunctionChoose/src/MLFeatures.arff"));
	    saver.writeBatch();
	    
	    
		FileWriter fstream = new FileWriter("C:/EclipseWorkspace/weka-3-8-0/weka-3-8-0/data/TEST-breast-cancer.arff");
		BufferedWriter out = new BufferedWriter(fstream);		
		train =new ArffReader(new BufferedReader(new FileReader("C:/EclipseWorkspace/DynamicFunctionChoose/src/MLFeatures.arff"))).getData();
		train.setClassIndex(train.numAttributes() - 1);
		Instances detectionSet =new ArffReader(new BufferedReader(new FileReader("C:/EclipseWorkspace/DynamicFunctionChoose/src/MLFeatures.arff"))).getData();
		detectionSet.setClassIndex(detectionSet.numAttributes() - 1);
		System.out.println("Sets are loaded.");
		
		//RotationForest Classifier = new weka.classifiers.meta.RotationForest();
		weka.classifiers.trees.J48 Classifier = new weka.classifiers.trees.J48();
		
		NumericToNominal convert= new NumericToNominal();
        String[] options= new String[2];
        options[0]="-R";
        options[1]="24";  //range of variables to make numeric

        convert.setOptions(options);
        convert.setInputFormat(train);

        Instances newData=Filter.useFilter(train, convert);
        
		
		int[] indicesOfColumnsToUse = new int[]{1,2,3,5,6,7,8,9,12,13,23};
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(indicesOfColumnsToUse);
		remove.setInvertSelection(true);
		remove.setInputFormat(newData);
		Instances trainingSubset = Filter.useFilter(newData, remove);		
		trainingSubset.setClassIndex(trainingSubset.numAttributes() - 1);
		
		Classifier.buildClassifier(trainingSubset);
		System.out.println("Classifier is ready.");
		weka.core.SerializationHelper.write("C:/EclipseWorkspace/weka-3-8-0/weka-3-8-0/data/j48.model", Classifier);
		
		// deserialize model
		 Classifier cls = (Classifier) weka.core.SerializationHelper.read("C:/EclipseWorkspace/weka-3-8-0/weka-3-8-0/data/j48.model");
		 
		 
		 Evaluation eval = new Evaluation(trainingSubset);
		 
		 eval.evaluateModel(cls, trainingSubset);
		 System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		
		out.close();
		}
}


