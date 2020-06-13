package ResultsReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Datahandler {
	private String logs;
	private ArrayList<String> files;
	
	public Datahandler(String nfilename) {
		logs = nfilename;
		files = new ArrayList<>();
	}
	
	public void getlogs(){
		File file = new File(logs);
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
		return files;
	}
	
}
