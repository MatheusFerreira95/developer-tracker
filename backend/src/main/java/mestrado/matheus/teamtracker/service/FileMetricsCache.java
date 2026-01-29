package mestrado.matheus.teamtracker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mestrado.matheus.teamtracker.domain.Developer;

/**
 * Cache em memória para armazenar métricas de desenvolvedores por arquivo.
 * Evita recalcular LOC e commits para o mesmo arquivo no mesmo checkout.
 * 
 * Chave do cache: localRepository + "|" + checkout + "|" + filePath
 * Valor: Lista de desenvolvedores com LOC e commits calculados
 */
public class FileMetricsCache {
	
	private static final Map<String, List<Developer>> cache = new HashMap<>();
	
	/**
	 * Gera a chave do cache baseada no repositório, checkout e caminho do arquivo.
	 */
	public static String generateKey(String localRepository, String checkout, String filePath) {
		String normalizedPath = (filePath == null || filePath.isEmpty()) ? "ROOT" : filePath;
		return localRepository + "|" + checkout + "|" + normalizedPath;
	}
	
	/**
	 * Obtém métricas do cache se existirem.
	 * 
	 * @param localRepository Caminho do repositório local
	 * @param checkout Branch ou tag do checkout
	 * @param filePath Caminho do arquivo (null para projeto inteiro)
	 * @return Lista de desenvolvedores ou null se não estiver em cache
	 */
	public static List<Developer> get(String localRepository, String checkout, String filePath) {
		String key = generateKey(localRepository, checkout, filePath);
		return cache.get(key);
	}
	
	/**
	 * Armazena métricas no cache.
	 * 
	 * @param localRepository Caminho do repositório local
	 * @param checkout Branch ou tag do checkout
	 * @param filePath Caminho do arquivo (null para projeto inteiro)
	 * @param developers Lista de desenvolvedores com métricas calculadas
	 */
	public static void put(String localRepository, String checkout, String filePath, List<Developer> developers) {
		String key = generateKey(localRepository, checkout, filePath);
		cache.put(key, developers);
	}
	
	/**
	 * Verifica se existe métricas em cache para o arquivo.
	 */
	public static boolean contains(String localRepository, String checkout, String filePath) {
		String key = generateKey(localRepository, checkout, filePath);
		return cache.containsKey(key);
	}
	
	/**
	 * Limpa o cache. Útil para testes ou quando necessário liberar memória.
	 */
	public static void clear() {
		cache.clear();
	}
	
	/**
	 * Remove entradas do cache relacionadas a um repositório específico.
	 * Útil quando o repositório é atualizado ou removido.
	 */
	public static void clearForRepository(String localRepository) {
		cache.entrySet().removeIf(entry -> entry.getKey().startsWith(localRepository + "|"));
	}
	
	/**
	 * Retorna o tamanho atual do cache.
	 */
	public static int size() {
		return cache.size();
	}
}
