package monitor;
import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;


public class Persistence implements IAlgoritmo {

	private RConnection connection;
	/**
	 * Configuracao dos parametros
	 */
	private StringBuffer configuracao;

	public Persistence() {
		super();

	}

	@Override
	/**
	 * Principal metodo do algoritmo é responsavel por fazer a chamada do Algoritmo em R e construir uma lista de arquivos de  saida(Contrato padrao)
	 */
	public String[] executar() throws Exception {

		System.out.println("Executing Persistence");
		connection = Util.criaConexao();
		REXP ret = null;

		try {

//			connection.eval("source('~/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Routines/Persistence.R')");
//			connection.eval("source('D:/AN-CV/KYOTO/Routines/Persistence.R')");
			
			ret = connection.parseAndEval(this.configuracao.toString());
			
		} catch (REngineException | REXPMismatchException e) {
			throw new Exception("Erro na execução da rotina Persistence!\n Chamada: " + this.configuracao.toString(), e);
		} finally {
			connection.close();
		}

		return ret.asStrings();
	}

	@Override
	/**
	 * Metodo responsavel por configurar a chamada em R.
	 */
	public void configurar(String[] parametros) throws Exception {
		this.configuracao = new StringBuffer();

		this.configuracao.append("Persistence(");
		this.configuracao.append("'" + parametros[0] + "'"); // GSitDate
		this.configuracao.append(",");
		this.configuracao.append(parametros[1] ); // GSit
		this.configuracao.append(")");
	}

	public StringBuffer getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(StringBuffer configuracao) {
		this.configuracao = configuracao;
	}

	public String mountOutputDirFileName(String date) {
//		String baseDir = "C:\\Users\\Carlos\\Dropbox\\KYOTO\\OUTPUTS";
//		String baseDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\OUTPUTS\\";
//		String baseDir = "D:\\Dropbox\\KYOTO\\OUTPUTS";

		String baseDir = "C:\\Users\\Carlos\\Dropbox\\AN-CV\\3 DATA\\6 KYOTO\\RESULTS PERS AND ARIMA";

		
		String algorithm = "PERSISTENCE";
		String label = "[Kyoto]";
		String period = "[FRCST-PRST-AVG-15]";
		String day = date.substring(0, date.length() - 9);
		String month = date.substring(0, day.length() - 3);
		String extension = ".txt";
		
		return baseDir + "\\" +
				algorithm + "\\" +
				label + month + period + "\\" +
				label + day + period + extension;
	}

	@Override
	public String getOutputHeader() {

		return "Tm" +
				"\t" +
				"Csky"+
				"\t" +
				"GSi"+
				"\t" +
				"Kt"+
				"\t" +
				"CSkyNext" +
				"\t" +
				"Perst";
	}
	
	
	public void gravarSaida(String[] saida) throws Exception{
		
		File outputFile = new File(mountOutputDirFileName(saida[0]));
		boolean written = false;
		
		if(outputFile.exists()){
			String lastLineOutput = Util.getLastLine(outputFile);
			
			//Testa se a linha ja foi escrita no arquivo
			if(lastLineOutput.contains(saida[0])){
				written = true;
			}
		}
		else{
			FileUtils.writeStringToFile(outputFile, getOutputHeader() + "\n", true);
		}
		
		if(!written){
			FileUtils.writeStringToFile(outputFile, StringUtils.join(saida, "\t") + "\n", true);
		}
	}
}
