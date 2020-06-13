package bivariate_colgen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import bivariate_colgen.Datahandler;
import bivariate_colgen.MIP;
import gurobi.GRBException;

public class MainCG {

	public static void main(String[] args) throws IOException, GRBException{
		
		ExperimentGenerator exps = new ExperimentGenerator("./data/02experiments.txt"); //Para n = 1000
		//ExperimentGenerator exps = new ExperimentGenerator("./data/04experiments.txt"); // Para n = 500,1000,2000,3000,5000 (sin exp -0.8)
		//ExperimentGenerator exps = new ExperimentGenerator("./data/03experiments.txt"); // Para n = 500,1000,2000,3000,5000 ( exp -0.8)
		exps.getExperiments();
		ArrayList<String> names = new ArrayList<>();
		ArrayList<Results> output = new ArrayList<>();
		names = exps.getFiles();
		
		
		PrintWriter writer = new PrintWriter("lognormales_traslape.txt", "UTF-8");	
		String linea = new String();
		linea = "Instance \t Integrality \t GeneratedCols \t Obj \t AchievedRho \t LRtime(ms) \t MIPtime(ms)";
		writer.println(linea);
	
		/*
		PrintWriter writer = new PrintWriter("toyresults1.txt", "UTF-8");	
		String linea = new String();
		linea = "Instance \t Rho_S \t Rho_P ";
		writer.println(linea);
		*/
		
		System.out.println(names.size());
		for (int k = 0; k < names.size(); k++) {
			System.out.println(k);
			Datahandler data = new Datahandler(names.get(k));
			data.readInstance();
			Results res = new Results(names.get(k));
			
			MIP mp = new MIP(data);
			
			boolean[][] added = new boolean[data.getN()][data.getN()];
			for (int i = 0; i < added.length; i++) {
				added[i][i] = true;
			}
			
			//long t_now = System.currentTimeMillis();
			mp.BuildModel();
			//String mp_sol = mp.Solve();
			//System.out.println(mp_sol);
			long t_now = System.currentTimeMillis();
			boolean continuar = true;
			int iter = 0;
			while (continuar){
				mp.Solve();
				double[] duals_iter = mp.getDuals(); 
				double[][] r_cost = mp.reducedCosts(added, duals_iter[duals_iter.length-1], Arrays.copyOfRange(duals_iter, 0, data.getN()), Arrays.copyOfRange(duals_iter, data.getN(), 2*data.getN()));
				ArrayList<int[]> in_vars = new ArrayList<>();
				in_vars = mp.getVars(r_cost);
				
				for (int i = 0; i < in_vars.size(); i++) {
					added[in_vars.get(i)[0]][in_vars.get(i)[1]] = true;
				}
				if(in_vars.size()>0){
					mp.update(in_vars);
				}else{
					continuar = false;
				}
				iter++;
				res.setNum_cols(iter);
			}
			System.out.println("Acabo de iterar");
			mp.Solve();
			long t_now2 = System.currentTimeMillis();
			long time = t_now2-t_now;
			res.setTimeRelax(time);
			if(!mp.checkIntegrality()){
				mp.forceIntegrality();
				//System.out.println("Relaxation is not integral");
				res.setIntegral(0);
				mp.Solve();
				long t_now3 = System.currentTimeMillis();
				long all_time = t_now3 - t_now2;
				res.setTimeMIP(all_time);
			}else{
				//System.out.println("Relaxation is integral");
				res.setIntegral(1);
				res.setTimeMIP(0);
			}
			
			res.setRho(mp.ReportSol());
			//System.out.println(mp.ReportSol());
			//System.out.println(mp.getObjVal());
			res.setObj(mp.getObjVal());
			System.out.println("Tiempo: "+ (res.getTimeMIP()+res.getTimeRelax()));
			output.add(res); 
			linea = new String();
			linea = res.getExperiment() + "\t";
			linea = linea + res.getIntegral() + "\t";
			linea = linea + res.getNum_cols() + "\t";
			linea = linea + res.getObj() + "\t";
			linea = linea + res.getRho() + "\t";
			linea = linea + res.getTimeRelax() + "\t";
			linea = linea + res.getTimeMIP() + "\t";
			writer.println(linea);
			/*
			linea = new String();
			linea = names.get(k)+"\t";
			linea = linea + data.getRhos() + "\t";
			linea = linea + data.getRhop();
			writer.println(linea);
			*/
		}
		writer.close();
		
		/*PrintWriter writer = new PrintWriter("toyresults.txt", "UTF-8");	
		//String linea = new String();
		//linea = "Instance \t Integrality \t GeneratedCols \t Obj \t AchievedRho \t time(ms)";
		//writer.println(linea);
		for (int i = 0; i < output.size(); i++) {
			linea = new String();
			linea = output.get(i).getExperiment() + "\t";
			linea = linea + output.get(i).getIntegral() + "\t";
			linea = linea + output.get(i).getNum_cols() + "\t";
			linea = linea + output.get(i).getObj() + "\t";
			linea = linea + output.get(i).getRho() + "\t";
			linea = linea + output.get(i).getTimeRelax();
			writer.println(linea);
		}
		writer.close();
		*/
		/*
		//Datahandler data = new Datahandler("./data/exp_exp_IC_corr_3_sample_1.txt");
		Datahandler data = new Datahandler("../Java/data/unif_unif_IND_corr_5_sample_9_size_5000.txt");
		data.readInstance();
		Results res = new Results("exp_exp_IC_corr_3_sample_1");
		
		MIP mp = new MIP(data);
		
		boolean[][] added = new boolean[data.getN()][data.getN()];
		for (int i = 0; i < added.length; i++) {
			added[i][i] = true;
		}
		
		//long t_now = System.currentTimeMillis();
		mp.BuildModel();
		//String mp_sol = mp.Solve();
		//System.out.println(mp_sol);
		long t_now = System.currentTimeMillis();
		boolean continuar = true;
		int iter = 0;
		while (continuar){
			mp.Solve();
			double[] duals_iter = mp.getDuals(); 
			double[][] r_cost = mp.reducedCosts(added, duals_iter[duals_iter.length-1], Arrays.copyOfRange(duals_iter, 0, data.getN()), Arrays.copyOfRange(duals_iter, data.getN(), 2*data.getN()));
			ArrayList<int[]> in_vars = new ArrayList<>();
			in_vars = mp.getVars(r_cost);
			for (int i = 0; i < in_vars.size(); i++) {
				added[in_vars.get(i)[0]][in_vars.get(i)[1]] = true;
			}
			if(in_vars.size()>0){
				mp.update(in_vars);
			}else{
				continuar = false;
			}
			iter++;
			res.setNum_cols(iter);
			System.out.println(iter);
		}
		
		mp.Solve();
		if(!mp.checkIntegrality()){
			mp.forceIntegrality();
			System.out.println("Relaxation is not integral");
			res.setIntegral(0);
			mp.Solve();
		}else{
			System.out.println("Relaxation is integral");
			res.setIntegral(1);
		}
		
		res.setRho(mp.ReportSol());
		System.out.println(mp.ReportSol());
		System.out.println(mp.getObjVal());
		res.setObj(mp.getObjVal());
		long t_now2 = System.currentTimeMillis();
		long time = t_now2-t_now;
		res.setTime(time);
		System.out.println("Tiempo: "+ time);
		*/		
	}
	
}
