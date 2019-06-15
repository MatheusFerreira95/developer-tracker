
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

class TruckFactor {

	static Map<String, List<String>> authors = new HashMap<String, List<String>>();
	static Map<String, List<String>> files = new HashMap<String, List<String>>();

	public static void main(String[] args) throws FileNotFoundException {

		readInputFile("/home/matheus/Área de trabalho/input.txt");
		greedyTruckFactor();

	}

	/**
	 * Método responsável por carregar e organizar os dados do arquivo de entrada
	 **/
	private static void readInputFile(String inputFileName) throws FileNotFoundException {

		System.out.println("Reading input file...");

		File intputFile = new File(inputFileName);

		Scanner scanner = new Scanner(intputFile);

		String fileName = null;

		//lendo cada linha do arquivo de entrada e carregando para as listas de autores e de arquivos
		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			if (getFileName(line) != null) {

				fileName = getFileName(line);
				continue;
			}

			String authorName = line;

			setAuthor(authorName, fileName);
		}

		//ordenando a lista de autores de forma que o maior autor seja o primeiro da lista
		orderAuthors();

	}

	/** Método que implementa o Algoritmo Guloso que calcula o valor de truck factor **/
	private static void greedyTruckFactor() {

		System.out.println("Calculating Truck Factor...");

		//lista de autores que foram retirados, ou seja, que entram no cálculo do truck factor
		List<String> authorsTF = new ArrayList<String>();
		
		//variável que guarda a quantidade que representa a metade dos arquivos totais do projeto
		Integer halfFiles = files.size() / 2;
		
		for (Map.Entry<String, List<String>> author : authors.entrySet()) {

			//adicionando autor à lista de truck factor
			authorsTF.add((String) author.getKey());

			//removendo autoria do autor dos arquivos do projeto e removendo arquivos orfaos
			removeFiles(author);

			//verificando se mais da metade dos arquivos são orfaos
			if (files.size() < halfFiles) {

				//parando execução do algoritmo e retornando resultado
				printTruckFactor(authorsTF);
				break;
			}
		}

	}

	/** ------------------------------------------ **/
	/** Métodos auxiliares **/
	/** ------------------------------------------ **/

	/**
	 * Método para ordenar a lista de autores, de forma que o autor de mais arquivos
	 * seja o primeiro da lista
	 **/
	private static void orderAuthors() {
		System.out.println("Order list authors...");

		Comparator<Entry<String, List<String>>> valueComparator = (e1,
				e2) -> e1.getValue().size() > e2.getValue().size() ? -1
						: e1.getValue().size() < e2.getValue().size() ? 1 : 0;

		Map<String, List<String>> sortedMap = authors.entrySet().stream().sorted(valueComparator)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		authors = sortedMap;
	}

	/** Método para imprimir o resultado final **/
	private static void printTruckFactor(List<String> authorsTF) {

		System.out.println("TF: " + authorsTF.size());
		System.out.println("Developer(s):");

		for (String author : authorsTF) {

			System.out.println(author);
		}
	}

	/**
	 * Método que remove os arquivos autores de arquivos e, ainda remove os arquivos
	 * que se tornaram 'órfãos'
	 **/
	private static void removeFiles(Entry<String, List<String>> author) {

		List<String> remove = new ArrayList<String>();

		Iterator<Entry<String, List<String>>> it = files.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> file = it.next();
			// remove author from file
			List<String> authorsFile = (List<String>) file.getValue();
			for (String authorFile : authorsFile) {

				if (authorFile.equals(author)) {
					authorsFile.remove(authorFile);
				}
			}
			// remove file with zero authors
			if (authorsFile.size() == 0) {
				remove.add(file.getKey());
			}
		}

		for (String key : remove) {
			files.remove(key);
		}
	}

	/**
	 * Método que preenche a lista de autores (adicionando os arquivos de sua
	 * autoria) e a lista de arquivos (adicionando os autores responsáveis por sua
	 * autoria)
	 **/
	private static void setAuthor(String authorName, String fileName) {

		// set list files
		List<String> authorsFile = files.get(fileName);

		if (authorsFile != null) {

			authorsFile.add(authorName);

		} else {

			files.put(fileName, new ArrayList<String>());
		}

		// set list authors
		List<String> filesAuthor = authors.get(authorName);

		if (filesAuthor != null) {

			filesAuthor.add(fileName);

		} else {

			authors.put(authorName, new ArrayList<String>());
		}
	}

	private static String getFileName(String line) {

		if (line.indexOf("FILE: ") == 0) {

			return line.substring(6);
		}

		return null;
	}

}
