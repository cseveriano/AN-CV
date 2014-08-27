package monitor;
import java.util.List;

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
	public String executar() throws Exception {

		connection = Util.criaConexao();
		REXP ret = null;

		try {

			ret = connection.parseAndEval(this.configuracao.toString());

		} catch (REngineException | REXPMismatchException e) {
			throw new Exception("Erro na execução da rotina Persistence!\n Chamada: " + this.configuracao.toString(), e);
		} finally {
			connection.close();
		}

		return ret.asString();
	}

	@Override
	/**
	 * Metodo responsavel por configurar a chamada em R.
	 */
	public void configurar(List<String> parametros) throws Exception {
		this.configuracao = new StringBuffer();

		this.configuracao.append("Persistence(");
		this.configuracao.append("'" + parametros.get(0) + "'");
		this.configuracao.append(",");
		this.configuracao.append("'" + parametros.get(1) + "'");
		this.configuracao.append(")");
	}

	public StringBuffer getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(StringBuffer configuracao) {
		this.configuracao = configuracao;
	}

}
