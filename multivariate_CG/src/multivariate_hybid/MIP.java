package multivariate_hybid;

import java.util.ArrayList;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MIP {
	
	private GRBEnv env;
	public GRBModel model;
	
	private Datahandler data;
	private int columna;
	private int[][] solucion_actual;
	
	/**
	 * x_ij Decision variable representing the assignment of observation i of first random vector to observation j of second random vector
	 */
	private ArrayList<GRBVar> x;
	/**
	 * Positive superavit in the correlation constraint.
	 */
	public GRBVar[] delta1;
	/**
	 * Positive slack in the correlation constraint.
	 */
	public GRBVar[] delta2;
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
	public GRBConstr[] corr;
	/**
	 * Pearson correlation induced by the MIP on the sample
	 */
	public double[] rho_sample;
	/**
	 * Aux array list to keep track of added variables
	 */
	private ArrayList<int[]> vars;	
	
	
	public MIP(Datahandler nData, int nColumna, int[][] nSolucion){
		data = nData;
		columna = nColumna+1;
		solucion_actual = nSolucion;
		x = new ArrayList<>();
		delta1 = new GRBVar[columna];
		delta2 = new GRBVar[columna];
		ctrs1 = new GRBConstr[data.getN()];
		ctrs2 = new GRBConstr[data.getN()];
		corr = new GRBConstr[columna];
		vars = new ArrayList<>();	
	}
	
	public void BuildModel() throws GRBException{
		env = new GRBEnv();
		env.set(GRB.DoubleParam.TimeLimit, 500);
		env.set(GRB.IntParam.OutputFlag, 0);
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
		for (int i = 0; i < delta1.length; i++) {
			delta1[i] = model.addVar(0, 2, 1, GRB.CONTINUOUS, "delta_super"+i);
			delta2[i] = model.addVar(0, 2, 1, GRB.CONTINUOUS, "delta_slack"+i);
		}
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
		for (int i = 0; i < columna; i++) {
			exp = new GRBLinExpr();
			for (int j = 0; j < vars.size(); j++) {
				exp.addTerm(data.getC()[i][columna][solucion_actual[vars.get(j)[1]][i]][vars.get(j)[2]], x.get(j));
			}
			exp.addTerm(-1, delta1[i]);
			exp.addTerm(1, delta2[i]);
			corr[i] =  model.addConstr(exp, GRB.EQUAL, data.getRho()[i][columna], "target"+i);
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

	
	public ArrayList<int[]> getVars(boolean[][] is_in) throws GRBException {
		ArrayList<int[]> in_vars = new ArrayList<>();
		
		double[] sigma = new double[columna];
		for (int i = 0; i < sigma.length; i++) {
			sigma[i] = corr[i].get(GRB.DoubleAttr.Pi);
		}
		
		double[] u = new double[data.getN()];
		for (int i = 0; i < u.length; i++) {
			u[i] = ctrs1[i].get(GRB.DoubleAttr.Pi);
		}
		
		double[] v = new double[data.getN()];
		for (int i = 0; i < v.length; i++) {
			v[i] = ctrs2[i].get(GRB.DoubleAttr.Pi);
		}
		
				
		double[][] r = new double[data.getN()][data.getN()];
		for (int i = 0; i < data.getN(); i++) {
			for (int j = 0; j < data.getN(); j++) {
				if(!is_in[i][j]){
					r[i][j] = -u[i]-v[j];
					for (int h = 0; h < columna; h++) {
						r[i][j] = r[i][j] - data.getC()[h][columna][solucion_actual[i][h]][j]*sigma[h];
					}
				}else
					r[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		
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
		if(r[mejor_x[0]][mejor_x[1]]<-0.0001){
			in_vars.add(mejor_x);
		}
		return in_vars;
	}

	
	public void update(ArrayList<int[]> vars_in) throws GRBException {
		if(vars_in.size()>1){
			System.out.println("WTF");
		}
		
		int[] varindex = {vars.size(),vars_in.get(0)[0],vars_in.get(0)[1]};
		GRBVar newVar = model.addVar(0, 1, 0, GRB.CONTINUOUS, "x" + vars_in.get(0)[0]+","+vars_in.get(0)[1]);
		x.add(newVar);
		vars.add(varindex);
		model.update();
		model.chgCoeff(ctrs1[vars_in.get(0)[0]], newVar, 1);
		model.update();
		model.chgCoeff(ctrs2[vars_in.get(0)[1]], newVar, 1);
		model.update();
		
		for (int i = 0; i < corr.length; i++) {
			model.chgCoeff(corr[i], newVar, data.getC()[i][columna][solucion_actual[vars_in.get(0)[0]][i]][vars_in.get(0)[1]]);
			model.update();
		}
			
	}
	
	public boolean checkIntegrality() throws GRBException{
		for (int i = 0; i < vars.size(); i++) {
			if(x.get(i).get(GRB.DoubleAttr.X)<0.999 && x.get(i).get(GRB.DoubleAttr.X)>0.001){
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
		model.set(GRB.DoubleParam.MIPGap, 0.01);
		//env.set(GRB.IntParam.OutputFlag, 1);
		model.update();
	}	
	
	public int[] reportSolution() throws GRBException{
		
		int[] solucion = new int[data.getN()]; 
		
		for (int i = 0; i < vars.size(); i++) {
			if(x.get(i).get(GRB.DoubleAttr.X)>0.9){
				solucion[vars.get(i)[1]] = vars.get(i)[2];			
			}
		}			
		return solucion;
	}
	
	public int getnumVars(){
		return this.vars.size();
	}
	
}
