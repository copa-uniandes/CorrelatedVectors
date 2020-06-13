package bivariate_decomp;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import bivariate_decomp.Datahandler;

public class MIP {
	
	
	//private Hungarian hungaro;
	//private BBCallback bbcallback;
	private GRBEnv env;
	private GRBModel model;
	/**
	 * x_ij Decision variable representing the assignment of observation i of first random vector to observation j of second random vector
	 */
	private GRBVar[][] x;
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
	private double rho_sample;

	private Datahandler data;

	public MIP(Datahandler nData) {
		data  = nData;
		x = new GRBVar[data.getN()][data.getN()];
		ctrs1 = new GRBConstr[data.getN()];
		ctrs2 = new GRBConstr[data.getN()];
		//hungaro = new Hungarian(Datahandler.c);
		//bbcallback = new BBCallback(data, hungaro);
	}

	public void BuildModel(String filename) throws GRBException{
		env = new GRBEnv();
		if(data.getN()>3000){
			env.set(GRB.DoubleParam.TimeLimit, 2000);	
		}else{
			env.set(GRB.DoubleParam.TimeLimit, 1000);
		}
		env.set(GRB.IntParam.OutputFlag,1);
		env.set(GRB.IntParam.Presolve, 0);
		env.set(GRB.StringParam.LogFile, filename);
		//env.set(GRB.IntParam.Method, 2);
		//env.set(GRB.IntParam.Cuts, 2);
		model = new GRBModel(env);
		for (int i = 0; i < data.getN(); i++) {
			for (int j = 0; j < data.getN(); j++) {
				GRBVar newVar = model.addVar(0, 1, 0, GRB.BINARY, "x" + i+","+j);
				x[i][j] = newVar;
			}
		}
		model.update();
		
		delta1 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSuper");
		delta2 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSlack");
		model.update();
		
		GRBLinExpr exp;
		for (int i = 0; i < data.getN(); i++) {
			exp = new GRBLinExpr();
			for (int j = 0; j < data.getN(); j++) {
				exp.addTerm(1, x[i][j]);
			}
			ctrs1[i] = model.addConstr(exp, GRB.EQUAL, 1, "salida " + i);
		}
		model.update();

		
		for (int i = 0; i < data.getN(); i++) {
			exp = new GRBLinExpr();
			for (int j = 0; j < data.getN(); j++) {
				exp.addTerm(1, x[j][i]);
			}
			ctrs2[i] = model.addConstr(exp, GRB.EQUAL, 1, "entrada " + i);
		}
		model.update();
		
		exp = new GRBLinExpr();
		for (int i = 0; i < data.getN(); i++) {
			for (int j = 0; j < data.getN(); j++) {
				exp.addTerm(data.getC()[i][j]/data.getDenom(), x[i][j]);
			}
		}
		exp.addTerm(-1, delta1);
		exp.addTerm(1, delta2);
		
		corr =  model.addConstr(exp, GRB.EQUAL, data.getRho(), "target");
		model.update();
	}

	public String Solve() throws GRBException{
		model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		model.update();
		model.optimize();
		int status = model.get(GRB.IntAttr.Status);
		if (status == GRB.OPTIMAL ) {
			System.out.println("optimal");
			rho_sample = 0;
			for (int i = 0; i < data.getN(); i++) {
				for (int j = 0; j < data.getN(); j++) {
					if (x[i][j].get(DoubleAttr.X)>0.99) {
						rho_sample += data.getC()[i][j];
						//System.out.println(data.getObservations()[i][0] + " , " + data.getObservations()[j][1] + "\t" +j  );
						//System.out.println(i+" , "+ j);
					}
				}
			}
			return "rho: "+ rho_sample/data.getDenom();
		} else {
			System.out.println("no optimal");
			return "Infeasible model";
		}
		
	}
	
	public double getObjVal() throws GRBException {
		//model.write("test1.lp");
		return model.get(GRB.DoubleAttr.ObjVal);
	}

	public double ReportSol() throws GRBException{
		model.update();	
		rho_sample = 0;
		
		for (int i = 0; i < data.getN(); i++) {
			for (int j = 0; j < data.getN(); j++) {
				if (x[i][j].get(DoubleAttr.X)>0) {
					rho_sample =+ data.getC()[i][j]/data.getDenom();
					System.out.println(
							data.getObservations()[i][0]+" "+data.getObservations()[j][1]);
					
				}
			}
		}
		return rho_sample;
	}
	
	public double getGap() throws GRBException{
		model.update();
		return model.get(GRB.DoubleAttr.MIPGap);
	}
	
	public double getBestSol() throws GRBException{
		return model.get(GRB.DoubleAttr.ObjVal);
	}
	
	public int getStatus() throws GRBException{
		return model.get(GRB.IntAttr.Status);
	}
}

