import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateDataSet {
	
	public static void main(String[] args) throws IOException {		
		
		String pathToGraph = "C:/Users/t-amirub/OneDrive/MA/net/dblp/";
		String networkFileName = "com-dblp.ungraph.txt";		
		String outputPath = "C:/Users/t-amirub/OneDrive/MA/net/amazon/y/MLRealFeatures.txt";
		boolean printTimeLog = true;
		String graphID = "dblp";
		boolean lightVersion = true;
		
		if (false &args.length <5){
			System.out.println("Input parameteres: pathToGraph  networkFileName  outputPath  printTimeLog  graphID lightVersion=true");
		}
		else{			
			
			if (args.length > 0)
				pathToGraph = args[0];		
							
			if (args.length > 1)
				networkFileName = args[1];
			
			if (args.length > 2)
				outputPath = args[2];
			
			if (args.length > 3)
				printTimeLog = Boolean.parseBoolean(args[3]);		
			
			if (args.length > 4)
				graphID = args[4].replace('/', '.');
			
			if (args.length > 5)
				lightVersion = Boolean.parseBoolean(args[5]);
			
			System.out.println("pathToGraph: " + pathToGraph);
			System.out.println("networkFileName: " + networkFileName);			
			System.out.println("outputPath: " + outputPath);
			System.out.println("printTimeLog: " + printTimeLog);
			System.out.println("graphID: " + graphID);
			System.out.println("lightVersion: " + lightVersion);
		
			
			try{
				Files.readAllLines(Paths.get(pathToGraph + networkFileName));
			}catch(Exception e){
				System.out.println("Missing file!");
				return;}
			
			CalcFeatures CF = new CalcFeatures();
			double[] features = CF.processGraph(pathToGraph,networkFileName, printTimeLog, "LOG" + graphID, lightVersion);			
			String featuresAsString = graphID + "," +combine(features,"," );			
						
			double[] labels = CalcLabels.pullLabelsForGraph(pathToGraph);			
			
			String labelsAsString = combine(labels,"," );
			
			String line = featuresAsString + ",LABELS," + labelsAsString;
			
			System.out.println(line);
			
			appendToFile(outputPath,line);	
		}
	}	
	
	private static void appendToFile(String path, String msg) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));		
		out.println(msg);
		out.close();	
	}
	
	static String combine(double[] s, String glue)
	{
	  int k = s.length;
	  if ( k == 0 )
	  {
	    return null;
	  }
	  StringBuilder out = new StringBuilder();
	  out.append( s[0] );
	  for ( int x=1; x < k; ++x )
	  {
	    out.append(glue).append(s[x]);
	  }
	  return out.toString();
	}

}
