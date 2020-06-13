package bivariate_colgen;

public class Results {
	private String Experiment;
	private double TimeRelax;
	private double TimeMIP;
	private double rho;
	private double obj;
	private int num_cols;
	private int integral;
	
	public Results(String name){
		setExperiment(name);
	}

	public String getExperiment() {
		return Experiment;
	}

	public void setExperiment(String experiment) {
		Experiment = experiment;
	}

	public double getTimeRelax() {
		return TimeRelax;
	}

	public void setTimeRelax(double time) {
		TimeRelax = time;
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

	public double getNum_cols() {
		return num_cols;
	}

	public void setNum_cols(int num_cols) {
		this.num_cols = num_cols;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public double getTimeMIP() {
		return TimeMIP;
	}

	public void setTimeMIP(double timeMIP) {
		TimeMIP = timeMIP;
	}
}
