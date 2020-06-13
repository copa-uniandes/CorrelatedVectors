package bivariate_decomp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import bivariate_decomp.Results;
import bivariate_decomp.ExperimentGenerator;
import bivariate_decomp.Datahandler;
import gurobi.GRBException;

public class MainCVG {
	
	public static void main(String[] args) throws GRBException, IOException{
		
		ExperimentGenerator exps = new ExperimentGenerator("./data/02experiments.txt"); // Para n = 500,1000,2000,3000(sin exp -0.8)
		exps.getExperiments();
		ArrayList<String> names = new ArrayList<>();
		ArrayList<Results> output = new ArrayList<>();
		
		names = exps.getFiles();
		PrintWriter writer = new PrintWriter("MIPLogs.txt", "UTF-8");
		String linea = new String();
		linea = "file \t" + "log";
		writer.println(linea);
		
		PrintWriter writer2 = new PrintWriter("MIPResults.txt", "UTF-8");	
		String linea2 = new String();
		linea2 = "Instance \t Rho \t Deltas \t BestSol \t Gap \t Status";
		writer2.println(linea2);
		
		for (int k = 0; k < names.size(); k++) {
			Datahandler data = new Datahandler(names.get(k));
			data.readInstance();
			Results res = new Results(names.get(k));
			linea = new String();
			linea = names.get(k) + "\t" + names.get(k) + "_log.txt";
			writer.println(linea);
			MIP mp = new MIP(data);
			mp.BuildModel(names.get(k)+"_log.txt");
			mp.Solve();
			res.setObj(mp.getObjVal());
			res.setRho(mp.ReportSol());
			res.setBestsol(mp.getBestSol());
			res.setGap(mp.getGap());
			res.setStat(mp.getStatus());
			output.add(res);
			mp.ReportSol();
			linea2 = new String();
			linea2 = res.getExperiment() + "\t";
			linea2 = linea2 + res.getObj() + "\t";
			linea2 = linea2 + res.getRho() + "\t";
			linea2 = linea2 + res.getBestsol() + "\t";
			linea2 = linea2 + res.getGap() + "\t";
			linea2 = linea2 + res.getStat();
			writer2.println(linea2);
			System.out.println("----------------------------------------------------------------------------");
			System.out.println("Total: "+names.size());
			System.out.println("Voy en: "+k);
			System.out.println("----------------------------------------------------------------------------");
		}
		
		writer.close();
		writer2.close();
		
	}
		
}
