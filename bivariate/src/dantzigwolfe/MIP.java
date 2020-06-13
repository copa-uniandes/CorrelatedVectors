package dantzigwolfe;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;

import java.util.ArrayList;

import dantzigwolfe.Datahandler;

public class MIP {
	
	private GRBEnv env;
	public GRBModel model;
	/**
	 * lambda_p variable for each vertex
	 */
	private ArrayList<GRBVar> lambda;
	/**
	 * Positive superavit in the correlation constraint.
	 */
	public GRBVar delta1;
	/**
	 * Positive slack in the correlation constraint.
	 */
	public GRBVar delta2;
	/**
	 * Convexity constraint
	 */
	public GRBConstr conv_ctr;
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
		lambda = new ArrayList<>();
	}

	public void BuildModel() throws GRBException{
		env = new GRBEnv();
		env.set(GRB.DoubleParam.TimeLimit, 1000);
		env.set(GRB.IntParam.OutputFlag,0);
		env.set(GRB.IntParam.Presolve, 1);
		//env.set(GRB.IntParam.Method, 2);
		model = new GRBModel(env);
		
		delta1 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSuper");
		delta2 = model.addVar(0, 2, 1, GRB.CONTINUOUS, "deltaSlack");
		model.update();
				
		GRBVar newVar = model.addVar(0,1,0,GRB.CONTINUOUS,"lambda_0"); 
		lambda.add(newVar);
		model.update();
		
		
		int[][] initial_sol = new Hungarian(data.getC()).execute();
		double c_ini = 0;
		for (int i = 0; i < initial_sol.length; i++) {
			c_ini = c_ini + data.getC()[initial_sol[i][0]][initial_sol[i][1]];
		}	
				
		GRBLinExpr exp = new GRBLinExpr();
		exp.addTerm(c_ini,lambda.get(0));
		exp.addTerm(-1, delta1);
		exp.addTerm(1, delta2);
		corr =  model.addConstr(exp, GRB.EQUAL, data.getRho(), "target");
		model.update();
		
		GRBLinExpr exp1 = new GRBLinExpr();
		exp1.addTerm(1,lambda.get(0));
		conv_ctr =  model.addConstr(exp1, GRB.EQUAL, 1, "convexity");
		model.update();
				
	}

	public String Solve(Boolean arg) throws GRBException{
		model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		model.update();
		model.optimize();
		int status = model.get(GRB.IntAttr.Status);
		if (status == GRB.OPTIMAL ) {
			System.out.println(getObjVal());
			if (arg) {
				for (int i = 0; i < lambda.size(); i++) {
					if (lambda.get(i).get(DoubleAttr.X)>0) {
						System.out.println(i+" = "+lambda.get(i).get(DoubleAttr.X));
					}
				}		
			}
			rho_sample = data.getRho() - delta1.get(DoubleAttr.X) + delta2.get(DoubleAttr.X);
			return "rho: "+ rho_sample;
		} else {
			System.out.println("no optimal");
			return "Infeasible model";
		}
		
	}
	
	public double[] getDuals() throws GRBException {
		model.update();
		double[] duals = new double[2];
		duals[0] = corr.get(GRB.DoubleAttr.Pi);
		duals[1] = conv_ctr.get(GRB.DoubleAttr.Pi);
		return duals;
	}
	
	public void updateMP(double vertex_cost) throws GRBException{
		GRBVar newVar = model.addVar(0, 1, 0, GRB.CONTINUOUS, "lambda"+lambda.size());
		lambda.add(newVar);
		model.update();
		
		model.chgCoeff(corr, newVar, vertex_cost);
		model.chgCoeff(conv_ctr, newVar, 1);
		model.update();
		
	}
		
	public double getObjVal() throws GRBException {
		//model.write("test1.lp");
		return model.get(GRB.DoubleAttr.ObjVal);
	}

	public double ReportSol() throws GRBException{
		model.update();
		model.optimize();
		model.update();
		
		rho_sample = 0;
		
		for (int i = 0; i < lambda.size(); i++) {
			if (lambda.get(i).get(DoubleAttr.X)>0) {
				System.out.println(i+" = "+lambda.get(i).get(DoubleAttr.X));
			}
		}
		rho_sample = data.getRho() - delta1.get(DoubleAttr.X) + delta2.get(DoubleAttr.X);
		return rho_sample;
	}
	
	
}
