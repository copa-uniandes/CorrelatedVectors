package multivariate_hybid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import gurobi.GRBException;

public class Main {
	
	public static void main(String[] args) throws GRBException, FileNotFoundException, UnsupportedEncodingException{
		// Load the set of files for this experiment
		ExperimentGenerator exps = new ExperimentGenerator("./data/experimentos.txt");
		exps.getExperiments();
		ArrayList<String> names = new ArrayList<>();
		names = exps.getFiles();
		
		PrintWriter my_results_writer = new PrintWriter(exps.getFilename()+"_Data.txt", "UTF-8");
		
		// For all files in the experiment
		for (int i = 0; i < names.size(); i++) {
			
			// Read the instance
			Datahandler data = new Datahandler(names.get(i));
			try {
				data.readInstance();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Se rompió leyendo el archivo "+names.get(i));
				break;
			}
			System.out.println("Leí "+i);
			long t_now = System.currentTimeMillis();
			
			// we save the indices of the data in an array
			int[][] sol = new int[data.getN()][data.getK()]; 
			// the first variable is not going to change
			for (int j = 0; j < data.getN(); j++) {
				sol[j][0] = j;
			}
						
			//We are fixing one column of data at a time
			for (int j = 0; j < data.getK()-1; j++) {
								
				MIP mp = new MIP(data, j, sol);
				
				boolean[][] added = new boolean[data.getN()][data.getN()];
				for (int k = 0; k < added.length; k++) {
					added[k][k] = true;
				}
				for (int k = 0; k < added.length-1; k++) {
					added[k][k+1] = true;
				}
				
				mp.BuildModel();
				
				boolean continuar = true;

				//System.out.println("Empiezo a iterar");
				while(continuar){	
					mp.Solve();
					
					ArrayList<int[]> in_vars = new ArrayList<>();
					in_vars = mp.getVars(added);
					//System.out.println("En esta iteracion van a entrar "+in_vars.size());
					if(in_vars.size() == 0){// || mp.getnumVars() > 0.005*data.getN()*data.getN()){
						continuar = false;
					}else{
						added[in_vars.get(0)[0]][in_vars.get(0)[1]] = true;
						mp.update(in_vars);
					}
					
//					if(in_vars.size()>0){
//						added[in_vars.get(0)[0]][in_vars.get(0)[1]] = true;
//						mp.update(in_vars);
//					}else{
//						continuar = false;
//					}
				}
				
				//System.out.println("Acabo de iterar");
				mp.Solve();
				
				if(!mp.checkIntegrality()){
					mp.forceIntegrality();
					mp.Solve();
				}else{
					//System.out.println("entero en LP");
				}
				
				int[] temp_sol = new int[data.getN()];
				temp_sol = mp.reportSolution();
				
				for (int k = 0; k < temp_sol.length; k++) {
					sol[k][j+1] = temp_sol[k];
				}
				System.out.println("Agrego "+j);
			}
			
			long t_now2 = System.currentTimeMillis();
			long tiempo = t_now2 - t_now;
			
			data.printsolution(sol);
			my_results_writer.println(names.get(i)+ "\t" + tiempo);
			System.out.println(names.get(i)+ "\t" + tiempo);
			System.out.println("Termino "+ (i+1) + " de " + names.size());
		}
		my_results_writer.close();
		
	}
}
