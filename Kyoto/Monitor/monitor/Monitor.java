package monitor;


import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


public class Monitor {

	/**
	 * Arquivo e nome de pasta não podem possuir espaço em branco
	 */

	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	private boolean recursivo;
	private static Monitor monitor;
	private static Properties arquivoProperties;

	public static void main(String[] args) {

		try{
//			arquivoProperties = Util.getProperties();
	
	//			monitor = new Monitor(Paths.get(arquivoProperties.getProperty("diretorioPadrao") + File.separatorChar + arquivoProperties.getProperty("diretorioAlvos")), true, hashNomeDiretorioAlvo);
			String inputDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\INPUTS";
//			String inputDir = "C:\\Users\\Carlos\\Dropbox\\KYOTO\\INPUTS";
			monitor = new Monitor(Paths.get(inputDir), true);
			monitor.processarEventos();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	Monitor(Path dir, boolean recursivo) throws IOException {

		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursivo = recursivo;

		if (recursivo) {
			registrarDiretorioRecursivo(dir);
		} else {
			registrarDiretorio(dir);
		}
	}

	@SuppressWarnings("unchecked")
	/**
	 * So para fazer o cast
	 * @param event
	 * @return
	 */
	private <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Registra o diretorio com o watchService
	 */
	private void registrarDiretorio(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE,  ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Registra o diretorio e todos os subDiretorios
	 * 
	 * @param hashNomeDiretorioAlvo2
	 */
	private void registrarDiretorioRecursivo(final Path start){
		try {
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (dir.toAbsolutePath().toFile().exists()) {
						try {
							registrarDiretorio(dir);
						} catch (IOException e1) {
							System.out.println("Erro ao registrar diretorio" + dir.toAbsolutePath().toString());
						}
					}
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			System.out.println("Erro ao registrar diretorio");
		}
	}


	private void processarEventos() throws Exception {
		for (;;) {

			/**
			 * Espera o evento cadastrado acontecer
			 */
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			/**
			 * diretorio
			 */
			Path dir = keys.get(key);
			if (dir == null) {
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();
				WatchEvent<Path> ev = cast(event);
				/**
				 * Nome do arquivo/diretorio
				 */
				Path arquivoEvento = ev.context();

				/**
				 * Descobre o alvo pelo arquivo alvo do evento
				 */
				Path diretorio = dir.resolve(arquivoEvento);
//				String pastaDiretorioAlterado = diretorio.toString().substring(0, diretorio.toString().lastIndexOf("/"));
//				String diretorioCorrente = pastaDiretorioAlterado.substring(0, pastaDiretorioAlterado.toString().lastIndexOf("/"));

				String[] params = null;
				
				if(kind == ENTRY_MODIFY && Files.isRegularFile(diretorio, NOFOLLOW_LINKS)){
					String linha = Util.getLastLine(diretorio.toFile());
					
					params = linha.split("\t");
					
//					IAlgoritmo algoritmo = new Persistence();
					IAlgoritmo algoritmo = new Arima(diretorio.toFile());
					
					algoritmo.configurar(params);
					String[] saida = algoritmo.executar();
					gravarSaida(saida);
					
				}
				/**
				 * Para registrar novos diretorios criados quando o monitor já
				 * estava executando
				 */
				if (recursivo && (kind == ENTRY_CREATE)) {

					if (diretorio.toFile().exists() && Files.isDirectory(diretorio, NOFOLLOW_LINKS)) {
						registrarDiretorioRecursivo(diretorio);
					}

				}
			}

			/**
			 * Remove a chave se o diretorio não estiver mais acessivel
			 */
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				/**
				 * Se todos os diretorios estiverem sem "acesso" termina
				 */
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	private void gravarSaida(String[] saida) throws Exception{
		
		
		File outputFile = new File(mountDirFileName(saida[0]));
		boolean written = false;
		
		if(outputFile.exists()){
			String lastLineOutput = Util.getLastLine(outputFile);
			
			//Testa se a linha ja foi escrita no arquivo
			if(lastLineOutput.contains(saida[0])){
				written = true;
			}
		}
		
		if(!written){
			FileUtils.writeStringToFile(outputFile, StringUtils.join(saida, "\t") + "\n", true);
		}
	}

	private String mountDirFileName(String date) {
//		String baseDir = "C:\\Users\\Carlos\\Dropbox\\KYOTO\\OUTPUTS";
//		String algorithm = "PERSISTENCE";
		String algorithm = "ARIMA";
		String baseDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\OUTPUTS\\";
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

}

