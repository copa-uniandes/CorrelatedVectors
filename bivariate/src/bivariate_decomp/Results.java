package bivariate_decomp;

public class Results {
	private String Experiment;
	private double rho;
	private double obj;
	private int stat;
	private double gap;
	private double bestsol;
	
	public Results(String name){
		setExperiment(name);
	}

	public String getExperiment() {
		return Experiment;
	}

	public void setExperiment(String experiment) {
		Experiment = experiment;
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	public double getObj() {
		return obj;
	}

	public void setObj(double obj) {
		this.obj = obj;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	public double getGap() {
		return gap;
	}

	public void setGap(double gap) {
		this.gap = gap;
	}

	public double getBestsol() {
		return bestsol;
	}

	public void setBestsol(double bestsol) {
		this.bestsol = bestsol;
	}
}
