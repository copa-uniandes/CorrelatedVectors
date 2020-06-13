package ResultsReader;

import java.io.PrintWriter;
import java.util.ArrayList;

public class MainResults {

	public static void main(String[] args) throws Exception{
		
		Datahandler docs = new Datahandler("./MIPLogsAll.txt");
		docs.getlogs();
		ArrayList<String> names = new ArrayList<>();
		names = docs.getFiles();
		
		PrintWriter writer = new PrintWriter("MIPTimesAll.txt", "UTF-8");
		String linea = new String();
		linea = "file \t" + "LR Time \t" + "Total Time";
		writer.println(linea);
		
		for (int i = 0; i < names.size(); i++) {
			linea = new String();
			Logger log = new Logger(names.get(i));
			linea = names.get(i) + "\t" + log.getLR() +"\t" + log.getTT();
			writer.println(linea);
			System.out.println(log.getLR());
			System.out.println(log.getTT());
		}
		
		writer.close();
		
	}
}
