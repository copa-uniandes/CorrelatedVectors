package dantzigwolfe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Datahandler {

	private float rho;
	private int N;
	private double[][] observations;
	private double x_bar;
	private double y_bar;
	private double[][] c;
	private double denom;
	private double denomx;
	private double denomy;
	
	private String fileName;

	public Datahandler(String nfilename) {
		fileName = nfilename;
	}

	public void readInstance() throws IOException {
		File file = new File(fileName);
		BufferedReader buffRdr = new BufferedReader(new FileReader(file));

		String line = null;
		String[] splittedline = null;

		line = buffRdr.readLine();
		splittedline = line.split(":");
		setRho(Float.parseFloat(splittedline[1]));
		
		line = buffRdr.readLine();
		splittedline = line.split(":");
		setN(Integer.parseInt(splittedline[1]));

		setObservations(new double[getN()][2]);
		
		x_bar = 0;
		y_bar = 0;
		
		for (int i = 0; i < getN(); i++) {
			line = buffRdr.readLine();
			splittedline = line.split(",");
			getObservations()[i][0] = Double.parseDouble(splittedline[0]);
			x_bar += getObservations()[i][0];
			getObservations()[i][1] = Double.parseDouble(splittedline[1]);
			y_bar += getObservations()[i][1];
		}
		
		x_bar = x_bar/getN();
		y_bar = y_bar/getN();
		
		setC(new double[getN()][getN()]);
		for (int i = 0; i < getN(); i++) {
			for (int j = 0; j < getN(); j++) {
				getC()[i][j]=(getObservations()[i][0]-x_bar)*(getObservations()[j][1]-y_bar);
			}
		}
		
		denomx = 0;
		denomy = 0;
		
		for (int i = 0; i < getN(); i++) {
			denomx += Math.pow(getObservations()[i][0]-x_bar, 2);
			denomy += Math.pow(getObservations()[i][1]-y_bar, 2);
		}
		
		setDenom(Math.sqrt(denomx*denomy));
		
		for (int i = 0; i < getN(); i++) {
			for (int j = 0; j < getN(); j++) {
				getC()[i][j]=getC()[i][j]/getDenom();
			}
		}
		
		buffRdr.close();
		
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public double getDenom() {
		return denom;
	}

	public void setDenom(double denom) {
		this.denom = denom;
	}

	public double[][] getC() {
		return c;
	}

	public void setC(double[][] c) {
		this.c = c;
	}

	public float getRho() {
		return rho;
	}

	public void setRho(float rho) {
		this.rho = rho;
	}

	public double[][] getObservations() {
		return observations;
	}

	public void setObservations(double[][] observations) {
		this.observations = observations;
	}
	
	public double[][] multiplyC(double pi){
		double[][] newC = new double[getN()][getN()];
		for (int i = 0; i < getN(); i++) {
			for (int j = 0; j < getN(); j++) {
				newC[i][j] = -getC()[i][j]*pi;
			}
		}
		return newC;
	}
	

}
