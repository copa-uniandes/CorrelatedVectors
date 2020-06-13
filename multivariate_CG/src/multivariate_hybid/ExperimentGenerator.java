package multivariate_hybid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ExperimentGenerator {
	private String experiments;
	private ArrayList<String> files;
	
	public ExperimentGenerator(String nfilename) {
		experiments = nfilename;
		files = new ArrayList<>();
	}
	
	public void getExperiments(){
		File file = new File(experiments);
		try {
			BufferedReader buffRdr = new BufferedReader(new FileReader(file));			
			
			String linea= buffRdr.readLine();
			while(linea != null){
				files.add(linea);
				linea = buffRdr.readLine();
			}
			buffRdr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public ArrayList<String> getFiles(){
		return this.files;
	}
	
	public String getFilename(){
		return this.experiments;
	}

}
