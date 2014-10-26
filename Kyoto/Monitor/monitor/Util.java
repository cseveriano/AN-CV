package monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @version 1.0
 * @author davidson Classe com metodo utilitarios
 */
public class Util {

	private static Properties defaultProperties;

	public static synchronized Properties getProperties() throws Exception {

		if (defaultProperties == null) {
			defaultProperties = new Properties();
			try {
				defaultProperties.load(Util.class.getClassLoader().getResourceAsStream("util/monitor.properties"));
			} catch (IOException e) {
				throw new Exception("Erro de io ao ler properties padrao do algoritmo", e);
			}

		}

		return defaultProperties;

	}

	public static Properties recuperaPropertiesPacoteNome(String pacoteNome) throws Exception {
		Properties props = new Properties();
		try {
			props.load(Util.class.getClassLoader().getResourceAsStream(pacoteNome));
		} catch (IOException e) {
			throw new Exception("Erro de io ao ler properties padrao do algoritmo", e);
		}

		return props;

	}

	public static Properties recuperaPropertiesPorCaminho(String caminho) throws Exception {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(caminho));
		} catch (IOException e) {
			throw new Exception("Erro de io ao ler properties por caminho " + caminho, e);
		}

		return prop;
	}

	/**
	 * Metodo responavel por criar a conexao com o Rserve
	 * 
	 * @throws AlgoritmoException
	 */
	public static RConnection criaConexao() throws Exception {

		RConnection connection;
//		Properties prop = getProperties();
//
//		String serverAdress = prop.getProperty("server", "localhost");
//		int serverPort = Integer.valueOf(prop.getProperty("port", "6311"));

		String serverAdress = "localhost";
		int serverPort = 6311;

		connection = null;
		try {
			connection = new RConnection(serverAdress, serverPort);
		} catch (RserveException e) {
			throw new Exception("Error creating R connection", e);
		}

		return connection;
	}


	/**
	 * 
	 * @param prop
	 * @param caminhoGravacao
	 * @throws AlgoritmoException
	 */
	public static void arquivoPropiedadeParaDisco(Properties prop, String caminhoGravacao, boolean sobreEscreve) throws Exception {

		if (sobreEscreve) {
			try {
				prop.store(new FileOutputStream(caminhoGravacao), null);
			} catch (IOException e) {
				throw new Exception("Erro ao ler arquivo properties " + caminhoGravacao, e);
			}
		} else {
			try {
				if (!new File(caminhoGravacao).exists())
					prop.store(new FileOutputStream(caminhoGravacao), null);
				Properties novoProperties = Util.recuperaPropertiesPorCaminho(caminhoGravacao);

				for (Entry<Object, Object> obj : prop.entrySet()) {
					novoProperties.put(obj.getKey(), obj.getValue());
					novoProperties.store(new FileOutputStream(caminhoGravacao), null);
				}
			} catch (IOException e) {
				throw new Exception("Erro ao ler arquivo properties " + caminhoGravacao, e);
			}
		}

	}

	public static String createDirectory(String nameDir) throws Exception {

		File dir = new File(nameDir);

		if (!dir.exists()) {
			(dir).mkdirs();
		}

		if (!dir.exists()) {
			throw new Exception("Could not create folder " + nameDir);
		}

		return nameDir;
	}

	public static void deletarDiretorioHierarquicamente(String nomeDiretorio) throws Exception {

		File dir = new File(nomeDiretorio);

		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new Exception(e);
		}
	}


	public static long contaLinhasArquivo(String caminhoArquivo) throws Exception {
		int numeroLinhas;
		LineNumberReader lnr;

		lnr = new LineNumberReader(new FileReader(new File(caminhoArquivo)));
		lnr.skip(Long.MAX_VALUE);
		numeroLinhas = lnr.getLineNumber();
		lnr.close();

		return numeroLinhas;
	}
	
	public static String getLastLine(File file) throws IOException{
		List<String> lines = FileUtils.readLines(file);
		
		if(lines != null && lines.size() > 0){
			return lines.get(lines.size() - 1);
		}
		
		return null;
	}

	public static List<String> getLastLines(File file, int lineStart) throws IOException{
		List<String> lines = FileUtils.readLines(file);
		
		if(lines != null && lines.size() > 0){
			 int index = lineStart < lines.size()? lineStart : lines.size() - 1;
			return (List<String>) lines.subList(lines.size() - index, lines.size());
		}
		
		return null;
	}
}

