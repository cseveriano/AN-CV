package monitor;

import java.util.List;

/**
 * 
 * @author davidson
 * Interface que deve ser implementada por todas as classes de algoritmos do Plugin
 *
 */
public interface IAlgoritmo {
	
	/**
	 * Metodo responsavel por configurar o algoritmo para execu��o
	 * @param parametros
	 * @param arquivos
	 * @throws AlgoritmoException
	 */
	public void configurar(List<String> parametros) throws Exception;
	/**
	 * Metodo responsavel pela chamada/execu��o do algoritmo em si
	 * @return
	 * @throws AlgoritmoException
	 */
	public String executar() throws Exception;

}