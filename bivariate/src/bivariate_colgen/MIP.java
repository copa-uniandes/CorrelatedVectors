package bivariate_colgen;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import bivariate_colgen.Datahandler;
import java.util.ArrayList;


public class MIP {
	
	private GRBEnv env;
	public GRBModel model;
	
	/**
	 * x_ij Decision variable representing the assignment of observation i of first random vector to observation j of second random vector
	 */
	private ArrayList<GRBVar> x;
	/**
	 * Positive superavit in the correlation constraint.
	 */
	public GRBVar delta1;
	/**
	 * Positive slack in the correlation constraint.
	 */
	public GRBVar delta2;
	/**
	 * Constraints that assign one observation of Y to each observation of X
	 */
	private GRBConstr[] ctrs1;
	/**
	 * Constraints that assign one observation of X to each observation of Y
	 */
	private GRBConstr[] ctrs2;
	/**
	 * Target correlation constraint
	 */
	public GRBConstr corr;
	/**
	 * Pearson correlation induced by the MIP on the sample
	 */
	public double rho_sample;
	/**
	 * Aux array list to keep track of added variables
	 */
	private ArrayList<int[]> vars;	
	
	private Datahandler data;

	public MIP(Datahandler nData) {
		data  = nData;
		x = new ArrayList<>();
		ctrs1 = new GRBConstr[data.getN()];
		ctrs2 = new GRBConstr[data.getN()];
		vars = new ArrayList<>();
	}

	public void BuildModel() throws GRBException{
		env = new GRBEnv();
		env.set(GRB.DoubleParam.TimeLimit, 1000);
		env.set(GRB.IntParam.OutputFlag,0);
		//env.set(GRB.IntParam.Presolve, 1);
		//env.set(GRB.IntParam.Method, 2);
		//env.set(GRB.IntParam.Cuts, 2);
		model = new GRBModel(env);
		
		
		/**
		 * Variables (i,i)
		 */
		for (int i = 0; i < data.getN(); i++) {
			int[] varindex = {i,i,i};
			GRBVar newVar = model.addVar(0, 1, 0, GRB.CONTINUOUS, "x" + i+","+i);
			x.add(newVar);
			vars.add(varindex);
		}
		model.update();
		
		/**
		 * Variables (i,i+1)
		 */
		for (int i = 1; i < data.getN(); i++) {
			int[] varindex = {vars.size(),i-1,i};
			GRBVar newVar = model.addVar(0, 1, 0, GRB.CONTINUOUS, "x" +(i-1)+","+i);
			x.add(newVar);
			vars.add(varindex);
		}
		model.update();
		
		/**
		 * deltas
		 */
		delta1 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSuper");
		delta2 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSlack");
		model.update();
		
		
		/**
		 * Assignment constraints
		 */
		GRBLinExpr exp;
		for (int i = 0; i < data.getN(); i++) {
			exp = new GRBLinExpr();
			//exp.addTerm(1, x.get(i));
			ctrs1[i] = model.addConstr(exp, GRB.EQUAL, 1, "salida " + i);
			ctrs2[i] = model.addConstr(exp, GRB.EQUAL, 1, "entrada " + i);
		}
		model.update();
		
		for (int i = 0; i < vars.size(); i++) {
			model.chgCoeff(ctrs1[vars.get(i)[1]], x.get(i), 1);
			model.chgCoeff(ctrs2[vars.get(i)[2]], x.get(i), 1);
		}
		model.update();
		
		/**
		 * Correlation constraint
		 */
		exp = new GRBLinExpr();
		for (int i = 0; i < vars.size(); i++) {
			exp.addTerm(data.getC()[vars.get(i)[1]][vars.get(i)[2]]/data.getDenom(), x.get(i));
		}
		exp.addTerm(-1, delta1);
		exp.addTerm(1, delta2);
		corr =  model.addConstr(exp, GRB.EQUAL, data.getRho(), "target");
		model.update();
		model.write("test.lp");
	}

	public double[] getDuals() throws GRBException{
		double[] duals = new double[1+2*data.getN()];
		for (int i = 0; i < ctrs1.length; i++) {
			duals[i] = ctrs1[i].get(DoubleAttr.Pi);
			duals[i+ctrs1.length] = ctrs2[i].get(DoubleAttr.Pi);
		}
		duals[duals.length-1]=model.getConstrByName("target").get(DoubleAttr.Pi);
		return duals;
	} 
	
	public double[][] reducedCosts(boolean[][] is_in,double sigma, double[] u, double[] v){
		double[][] r = new double[data.getN()][data.getN()];
		for (int i = 0; i < data.getN(); i++) {
			for (int j = 0; j < data.getN(); j++) {
				if(!is_in[i][j]){
					r[i][j] = -data.getC()[i][j]*sigma/data.getDenom()-u[i]-v[j];
				}else
					r[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		return r;
	}
		
	public ArrayList<int[]> getVars(double[][] r){
		ArrayList<int[]> in_vars = new ArrayList<>();
		/*for (int i = 0; i < r.length; i++) {
			int mejor_j = 0;
			double mejor_rj = Double.POSITIVE_INFINITY;
			for (int j = 0; j < r[0].length; j++) {
				if(r[i][j]<mejor_rj){
					mejor_j = j;
					mejor_rj = r[i][j];
				}
			}
			if(r[i][mejor_j]<0){
				int[] new_var = new int[2];
				new_var[0] = i;
				new_var[1] = mejor_j;
				in_vars.add(new_var);
			}
		} */
		/*
		for (int j = 0; j < r[0].length; j++) {
			int mejor_i = 0;
			double mejor_ri = Double.POSITIVE_INFINITY;
			for (int i = 0; i < r.length; i++) {
				if(r[i][j] < mejor_ri){
					mejor_i = i;
					mejor_ri = r[i][j];
				}
			}
			if(r[mejor_i][j]<0){
				int[] new_var = new int[2];
				new_var[0] = mejor_i;
				new_var[1] = j;
				in_vars.add(new_var);
			}
		}*/
		int[] mejor_x = new int[2];
		double mejor_r = Double.POSITIVE_INFINITY;
		for (int i = 0; i < r.length; i++) {
			for (int j = 0; j < r[0].length; j++) {
				if(r[i][j]<mejor_r){
					mejor_x[0] = i;
					mejor_x[1] = j;
					mejor_r = r[i][j];
				}
			}
		}
		if(r[mejor_x[0]][mejor_x[1]]<0){
			in_vars.add(mejor_x);
		}
		return in_vars;
	}
	
	public ArrayList<int[]> getVars2(double[][] r){
		ArrayList<int[]> in_vars = new ArrayList<>();
		for (int i = 0; i < r.length; i++) {
			for (int j = 0; j < r[0].length; j++) {
				if(r[i][j]<0){
					int[] mejor_x = {i,j};
					in_vars.add(mejor_x);
					return in_vars;
				}
			}
		}
		return in_vars;
	}
	
	
	public void update(ArrayList<int[]> vars_in) throws GRBException{
		for (int i = 0; i < vars_in.size(); i++) {
			int[] varindex = {vars.size(),vars_in.get(i)[0],vars_in.get(i)[1]};
			GRBVar newVar = model.addVar(0, 1, 0, GRB.CONTINUOUS, "x" + vars_in.get(i)[0]+","+vars_in.get(i)[1]);
			x.add(newVar);
			vars.add(varindex);
			model.update();
			model.chgCoeff(corr, newVar, data.getC()[vars_in.get(i)[0]][vars_in.get(i)[1]]/data.getDenom());
			model.update();
			model.chgCoeff(ctrs1[vars_in.get(i)[0]], newVar, 1);
			model.update();
			model.chgCoeff(ctrs2[vars_in.get(i)[1]], newVar, 1);
			model.update();
			
		}
	}
	
	public String Solve() throws GRBException{
		model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		model.update();
		model.optimize();
		int status = model.get(GRB.IntAttr.Status);
		if (status == GRB.OPTIMAL ) {
			//System.out.println("optimal");
			return "fo: "+ this.getObjVal();
		} else {
			System.out.println("no optimal :(");
			return "Infeasible model";
		}
	}
	
	public double getObjVal() throws GRBException {
		return model.get(GRB.DoubleAttr.ObjVal);
	}

	public double ReportSol() throws GRBException{
		//model.optimize();
		//model.update();
		rho_sample = 0;
		for (int i = 0; i < x.size(); i++) {
			if (x.get(i).get(DoubleAttr.X)>0) {
				rho_sample += data.getC()[vars.get(i)[1]][vars.get(i)[2]]/data.getDenom();
				System.out.println(
						data.getObservations()[vars.get(i)[1]][0]+" "+data.getObservations()[vars.get(i)[2]][1]);
					
			}
		}
		//System.out.println(delta1.get(DoubleAttr.X)+delta2.get(DoubleAttr.X));
		return rho_sample;
	}
	
	public boolean checkIntegrality() throws GRBException{
		
		for (int i = 0; i < vars.size(); i++) {
			if(x.get(i).get(DoubleAttr.X)<0.999 && x.get(i).get(DoubleAttr.X)>0.001){
				return false;
			}
		}
		return true;
	}
	
	public void forceIntegrality() throws GRBException{
		for (int i = 0; i < x.size(); i++) {
			x.get(i).set(GRB.CharAttr.VType, GRB.BINARY);
		}
		model.update();
	}
	
}
