package ResultsReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Logger {
	private double LR_time;
	private double Total_time;
		
	public Logger(String nfilename) throws Exception {
		Boolean flag = true;
		File file = new File(nfilename);
		BufferedReader buffRdr = new BufferedReader(new FileReader(file));
		String linea= buffRdr.readLine();
		while(linea != null && flag){
			if(linea.indexOf("Root relaxation")!=-1){
				//System.out.println("LR: "+linea.substring(linea.indexOf("s,")+2, linea.indexOf("seconds")-1));
				LR_time = Double.parseDouble(linea.substring(linea.indexOf("s,")+2, linea.indexOf("seconds")-1));
			}
			if(linea.indexOf("Explored")!=-1){
				//System.out.println("TT: "+linea.substring(linea.indexOf("in")+2, linea.indexOf("seconds")-1));
				Total_time = Double.parseDouble(linea.substring(linea.indexOf("in")+2, linea.indexOf("seconds")-1));
				flag = false;
			}
			linea = buffRdr.readLine();
		}
		buffRdr.close();
	}
	
	public double getLR(){
		return LR_time;
	}
	
	public double getTT(){
		return Total_time;
	}
}
