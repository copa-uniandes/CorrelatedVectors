package multivariate_hybid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Datahandler {
	private double[][] rho;
	private int K;
	private int N;
	private double[][] observations;
	private double[][][][] c;
	private double[] medias;
	private double[] denoms;
	private String fileName;
	private double gap;
	
	
	public Datahandler(String nfilename) {
		fileName = nfilename;
	}
	
	public void readInstance() throws IOException {
		File file = new File(fileName);
		BufferedReader buffRdr = new BufferedReader(new FileReader(file));

		String line = null;
		String[] splittedline = null;
		
		//Leo y guardo k (el num. de RV)
		line = buffRdr.readLine();
		splittedline = line.split(":");
		setK(Integer.parseInt(splittedline[1]));
		
		//Leo y guardo N (el num. de observaciones)
		line = buffRdr.readLine();
		splittedline = line.split(":");
		setN(Integer.parseInt(splittedline[1]));
		
		//Leo y guardo la info de IC
		line = buffRdr.readLine();
		splittedline = line.split(":");
		setGap(Double.parseDouble(splittedline[1]));
		
		
		//Leo y mando al carajo el "Rho:"
		line = buffRdr.readLine();
		
		//Leo y guardo la matriz target
		setRho(new double[getK()][getK()]);
		for (int i = 0; i < getK(); i++) {
			line = buffRdr.readLine();
			splittedline = line.split(" ");
			for (int j = 0; j < splittedline.length; j++) {
				setRho(Double.parseDouble(splittedline[j]),i,j);
			}
		}
		
		//Leo y mando al carajo el "X:"
		line = buffRdr.readLine();
		
		//Preparo el array de medias
		setMedias(new double[getK()]);
		for (int i = 0; i < getK(); i++) {
			setMedias(0,i);
		}
		
		//Leo y guardo los x, y voy actualizando las medias
		setObservations(new double[getN()][getK()]);
		for (int i = 0; i < getN(); i++) {
			line = buffRdr.readLine();
			splittedline = line.split(",");
			for (int j = 0; j < splittedline.length; j++) {
				setObservations(Double.parseDouble(splittedline[j]),j,i);
				setMedias(getMedias()[j]+Double.parseDouble(splittedline[j]),j);
			}
		}
		buffRdr.close();

		//Calculo la media
		for (int i = 0; i < getK(); i++) {
			setMedias(getMedias()[i]/getN(),i);
		}
		
		//Matriz de costos
		
		//Primero los denominadores
		
		setDenoms(new double[getK()]);
		for (int i = 0; i < getK(); i++) {
			setDenoms(0,i);
		}
		
		for (int i = 0; i < getN(); i++) {
			for (int j = 0; j < getK(); j++) {
				double num = Math.pow(getObservations()[i][j]-getMedias()[j],2); 
				setDenoms(num+getDenoms()[j],j);
			}
		}
		
		// Ahora si la matriz
		
		setC(new double[getK()][getK()][getN()][getN()]);
		for(int i_1 = 0; i_1 <getN(); i_1++){
			for(int i_2 = 0; i_2 < getN(); i_2++){
				for(int k_1 = 0; k_1 < getK(); k_1++){
					for(int k_2 = 0; k_2 < getK(); k_2++){
						double num_1 = getObservations()[i_1][k_1] - getMedias()[k_1];
						double num_2 = getObservations()[i_2][k_2] - getMedias()[k_2];
						double denom = Math.sqrt(getDenoms()[k_1]*getDenoms()[k_2]);
						double costo = (num_1*num_2)/denom;
						//this.c[k_1][k_2][i_1][i_2]=costo;
						this.c[k_1][k_2][i_1][i_2]= new BigDecimal(Double.toString(costo)).setScale(4, RoundingMode.HALF_UP).doubleValue();
					}
				}
			}
		}
		
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}

	public double[][] getRho() {
		return rho;
	}

	public void setRho(double[][] rho) {
		this.rho = rho;
	}

	public double[][] getObservations() {
		return observations;
	}

	public void setObservations(double[][] observations) {
		this.observations = observations;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public double[] getMedias() {
		return medias;
	}

	public void setMedias(double[] medias) {
		this.medias = medias;
	}

	public double[] getDenoms() {
		return denoms;
	}

	public void setDenoms(double[] denoms) {
		this.denoms = denoms;
	}

	public double getGap() {
		return gap;
	}

	public void setGap(double gap) {
		this.gap = gap;
	}

	public void setObservations(double observation, int variable, int obs) {
		this.observations[obs][variable] = observation;
	}
	
	public void setC(double c, int var_1, int var_2, int obs_1, int obs_2) {
		this.c[var_1][var_2][obs_1][obs_2] = c;
	}

	public void setC(double[][][][] c) {
		this.c = c;
	}

	public double[][][][] getC() {
		return c;
	}
	
	public void setRho(double d, int i, int j) {
		this.rho[i][j] = d;
	}
	
	public void setMedias(double medias,int k) {
		this.medias[k] = medias;
	}
	
	public void setDenoms(double denom, int var) {
		this.denoms[var] = denom;
	}

	public static int indexOfArray(double[] array, double key) {
	    int returnvalue = -1;
	    for (int i = 0; i < array.length; ++i) {
	        if (key == array[i]) {
	            returnvalue = i;
	            break;
	        }
	    }
	    return returnvalue;
	}

	public void printsolution(int[][] sol) throws FileNotFoundException, UnsupportedEncodingException { 
		PrintWriter my_writer = new PrintWriter(this.fileName+"_Data.txt", "UTF-8");
		for (int i = 0; i < N; i++) {
			for(int j = 0; j < K; j++){
				my_writer.print(getObservations()[sol[i][j]][j]+"\t");
			}
			my_writer.println();
		}		
		my_writer.close();
	}
	

}
