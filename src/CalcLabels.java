import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CalcLabels {	
	
	public static double[] pullLabelsForGraph(String pathToGraph) throws IOException {	
		String[] metrics = {"NMI2", "OmegaIndex", "F1"};	
		double W_NMI =-4;	
		double W_OMEGA =-4;
		double W_F1 =-4;
		double W_sum = -4;
		double M_NMI =-4;	
		double M_OMEGA =-4;
		double M_F1 =-4;
		double M_sum = -4;
		String[] Mbetas = {"betta1.05.dat", "betta1.07.dat", "betta1.09.dat", "betta1.1.dat", "betta1.2.dat", "betta1.3.dat", "betta1.4.dat", "betta1.5.dat"};
		String[] Wbetas = {"betta1.1.dat", "betta1.3.dat", "betta1.5.dat", "betta2.5.dat", "betta3.5.dat", "betta4.5.dat", "betta1.2.dat", "betta1.4.dat", "betta2.0.dat", "betta3.0.dat", "betta4.0.dat"};
		String MName ="JavaOL";
		String WName ="JavaOSCD/merge";
		
		
		double[] wValues = FindBestResults(pathToGraph, WName, Wbetas, metrics);
		W_NMI = wValues[0];
		W_F1 = wValues[1];
		W_OMEGA = wValues[2];
		
		double[] mValues = FindBestResults(pathToGraph, MName, Mbetas, metrics);
		M_NMI = mValues[0];
		M_F1 = mValues[1];
		M_OMEGA = mValues[2];
		
		W_sum = W_NMI+W_OMEGA+W_F1;
		M_sum = M_NMI+M_OMEGA+M_F1;
				
		return new double[] {W_NMI,W_F1,W_OMEGA,W_sum,
							 M_NMI,M_F1,M_OMEGA,M_sum,
							 W_sum < 0 | M_sum < 0 ? -1 :
								 W_sum>=M_sum ? 1.0 : 0.0};
	}
	
	
	private static double[] FindBestResults(String pathToGraph, String algo, String[] betas, String[] metrics) {		
		double nmiBest =-4;
		double f1Best =-4;
		double omegaBest =-4;
		double sumBest = -4;
		for (String beta : betas ){
			double tmpNMI = -4;
			double tmpF1 = -4;
			double tmpOmega = -4;			
			double tmpSum = -4;
			for (String metric : metrics){
				boolean isNMI = metric == "NMI2";
				boolean isF1 = metric == "F1";				
				String pathToFile = pathToGraph + "metrics/" + metric + "/"+ algo + "/" + beta;				
				double value = extractValueFromFile(pathToFile,isNMI);
				if (isNMI) tmpNMI = value;
				else if (isF1) tmpF1 = value;
				else { tmpOmega = value;}				
			}
			tmpSum = tmpF1 + tmpNMI + tmpOmega;				
			if(sumBest < tmpSum){
				sumBest = tmpSum;
				nmiBest = tmpNMI;
				f1Best = tmpF1;
				omegaBest = tmpOmega;							
			}				
		}
		return new double[]{nmiBest, f1Best, omegaBest};
	}


	private static double extractValueFromFile(String pathToFile, boolean isNMI) {
		double val = -4;		 
		try{
			List<String> lines = Files.readAllLines(Paths.get(pathToFile));		
			int index= isNMI? 3 : 0;
			String line = lines.get(index);
			String Sval = line.split("\t")[1];
			Sval = Sval.replace("OmegaIndex:", "");
			Sval = Sval.replace("AverageF1Score:", "");
			val = Double.parseDouble(Sval);
			
		}catch(Exception e){return -4;}
		return val;
	}
}
