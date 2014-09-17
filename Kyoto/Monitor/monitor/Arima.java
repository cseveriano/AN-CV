package monitor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

			connection.eval("source('~/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Routines/Arima.R')");
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
				
				connection.assign("timeseries", line[1]);
				
				for (int i = 1; i < 2; i++) {
					line = series.get(i).split("\t");
					connection.assign("tmp", line[1]);
					connection.eval("timeseries<-rbind(timeseries,tmp)");
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

}
