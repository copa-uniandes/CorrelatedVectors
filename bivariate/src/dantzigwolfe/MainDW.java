package dantzigwolfe;

import java.io.IOException;
import java.util.Random;

//import dantzigwolfe.Datahandler;
//import dantzigwolfe.MIP;
import gurobi.GRBException;

public class MainDW {
	public static void main(String[] args) throws GRBException, IOException{
		
		Random rand = new Random();
		double[][] newC = new double[1000][1000];
		for (int i = 0; i < newC.length; i++) {
			for (int j = 0; j < newC.length; j++) {
				newC[i][j] = rand.nextInt(1000000);
			}
		}
		System.out.println("alo");
		long t_now = System.currentTimeMillis();
		int[][] initial_sol = new Hungarian(newC).execute();
		long t_now2 = System.currentTimeMillis();
		
		
		for (int i = 0; i < initial_sol.length; i++) {
			System.out.println(initial_sol[i][0]+" "+initial_sol[i][1]+" "+newC[initial_sol[i][0]][initial_sol[i][1]]);
		}
		
		long time = t_now2-t_now;
		System.out.println("Tiempo: "+ time);
		
		/*Datahandler data = new Datahandler("./data/1test2java.txt");
		data.readInstance();
		MIP mp = new MIP(data);
		long t_now = System.currentTimeMillis();
		mp.BuildModel();
		mp.Solve(false);
		
		
		
		double[] duals = new double[2];
		duals = mp.getDuals();
		double sigma = duals[1];
		int[][] initial_sol = new Hungarian(data.multiplyC(duals[0])).execute();
		double c_ini = 0;
		for (int i = 0; i < initial_sol.length; i++) {
			//System.out.println(initial_sol[i][0]+","+initial_sol[i][1]);
			c_ini = c_ini + data.getC()[initial_sol[i][0]][initial_sol[i][1]];
		}
		double reduced_cost = c_ini*duals[0];
		System.out.println(duals[0]);
		System.out.println(sigma);
		int iter=0;
		while (-reduced_cost < sigma) {
			System.out.println("iteración " + iter);
			mp.updateMP(c_ini);
			mp.Solve(false);
			duals = mp.getDuals();
			sigma = duals[1];
			c_ini = 0;
			initial_sol = new Hungarian(data.multiplyC(duals[0])).execute();
			for (int i = 0; i < initial_sol.length; i++) {
				c_ini = c_ini + data.getC()[initial_sol[i][0]][initial_sol[i][1]];
			}
			reduced_cost = duals[0]*c_ini;
			//System.out.println(-reduced_cost-sigma);
			iter++;
		}
		long t_now2 = System.currentTimeMillis();
		mp.ReportSol();
		//mp.model.write("test.lp");
		long time = t_now2-t_now;
		System.out.println("Tiempo: "+ time); */
	}
}
