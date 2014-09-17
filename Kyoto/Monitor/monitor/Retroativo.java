package monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class Retroativo {

	public static void main(String[] args) {

		try{
//			arquivoProperties = Util.getProperties();
	
	//			monitor = new Monitor(Paths.get(arquivoProperties.getProperty("diretorioPadrao") + File.separatorChar + arquivoProperties.getProperty("diretorioAlvos")), true, hashNomeDiretorioAlvo);
//			String inputDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\INPUTS";
			String inputDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\INPUTS";
			walk(inputDir);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
    private static void walk( String path ) throws IOException, Exception {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
				processFile(f);

            }
        }
    }

	private static void processFile(File f) throws IOException, Exception {
		
		try(BufferedReader br = new BufferedReader(new FileReader(f))) {
			
			String header = br.readLine();
			
		    for(String line; (line = br.readLine()) != null; ) {
				String[] params = line.split("\t");
				
				IAlgoritmo algoritmo = new Persistence();
				
				algoritmo.configurar(params);
				String[] saida = algoritmo.executar();
				gravarSaida(saida);
		    }
		}
	}
	
	private static void gravarSaida(String[] saida) throws Exception{
		
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

	private static String mountDirFileName(String date) {
		String baseDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\OUTPUTS";
		String algorithm = "PERSISTENCE";
//		String baseDir = "C:\\Users\\Carlos\\Documents\\Projetos Machine Learning\\ANN-CV\\CODES\\Git\\AN-CV\\Kyoto\\Data\\OUTPUTS\\";
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
