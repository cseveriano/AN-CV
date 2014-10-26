package monitor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;


public class Arima implements IAlgoritmo {

	private RConnection connection;
	/**
	 * Configuracao dos parametros
	 */
	private StringBuffer configuracao;
	private File dataFile;

	public Arima() {
		super();

	}

	public Arima(File file) {
		dataFile = file;
	}

	@Override
	/**
	 * Principal metodo do algoritmo é responsavel por fazer a chamada do Algoritmo em R e construir uma lista de arquivos de  saida(Contrato padrao)
	 */
	public String[] executar() throws Exception {

		connection = Util.criaConexao();
		REXP ret = null;

		try {

//			connection.eval("source('~/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Routines/Arima.R')");
			connection.eval("source('D:/AN-CV/KYOTO/Routines/Arima.R')");
			int seriesLength = 100;
			
			List<String> series = Util.getLastLines(dataFile, seriesLength);

			if(series == null || series.size() < seriesLength){
				
				String fileName = dataFile.getName(); 
				// Pegar a parte de data
				String datePart = fileName.substring(fileName.length()-22, fileName.length()-12);
				// Voltar 1 dia
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date date = formatter.parse(datePart);
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.DATE, -1);
				
				String newFilename = fileName.replace(datePart, formatter.format(cal.getTime()));
				
				File newFile = new File(dataFile.getParent() + "\\" + newFilename);
				
				int rest = series != null? seriesLength - series.size() : 0;
				
				List<String> restSeries = Util.getLastLines(newFile, rest);
				
				if(restSeries != null){
					restSeries.addAll(series);
					
					series = restSeries;
				}
						
			}
			
			if(series != null){
				String[] line = series.get(0).split("\t");
				
				connection.eval("timeseries <-"+ line[1]);
				
				for (int i = 1; i < series.size(); i++) {
					line = series.get(i).split("\t");
					connection.eval("timeseries<-rbind(timeseries,"+line[1]+")");
				}
			}

			ret = connection.parseAndEval(this.configuracao.toString());
			
		} catch (REngineException | REXPMismatchException e) {
			throw new Exception("Erro na execução da rotina Arima!\n Chamada: " + this.configuracao.toString(), e);
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

		this.configuracao.append("Arima('");
		this.configuracao.append(parametros[0]); // GSitDate
		this.configuracao.append("',");
		this.configuracao.append(parametros[1]); // GSit
		this.configuracao.append(",");
		this.configuracao.append("timeseries"); // GSitDate
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
//		String baseDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\OUTPUTS\\";
		String baseDir = "D:\\Dropbox\\KYOTO\\OUTPUTS";
		String algorithm = "ARIMA";
//		String algorithm = "ARIMA";
		String label = "[Kyoto]";
		String period = "[FRCST-ARIM-AVG-15]";
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
				"GSi" +
				"\t" +
				"Arim";
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
