
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

	// lista de autores com seus respectivos arquivos
	static Map<String, List<String>> authors = new HashMap<String, List<String>>();

	// lista de arquivos com seus respectivos autores
	static Map<String, List<String>> files = new HashMap<String, List<String>>();

	// lista de instâncias de entrada
	static List<String> instances = new ArrayList<String>(
			Arrays.asList("entradas/easy-vue.txt", "entradas/muse-ui.txt", "entradas/vue-apollo.txt",
					"entradas/vue-awesome-swiper.txt", "entradas/vue-quill-editor.txt", "entradas/vue-video-player.txt"));

	// string contendo código html que será utilizado para exibição do resultado
	static String htmlContent = "";

	// variáveis para guardar o tempo de execução de cada algoritmo utilizado
	static Instant startReadInputFile;
	static Instant finishReadInputFile;
	static Instant startOrder;
	static Instant finishOrder;
	static Instant startGreedyTruckFactor;
	static Instant finishGreedyTruckFactor;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		// guardando momento inicial do processamento
		Instant start = Instant.now();

		// caso o usuário passe uma instância como parâmetro ela será considerado.
		// Caso contrário, as instâncias pré-configuradas serão utilizadas
		String inputFileName = args.length > 0 ? args[0] : null;
		if (inputFileName != null) {
			instances = new ArrayList<String>(Arrays.asList(inputFileName));
		}

		// criando um identificador para cada instância.
		// Isso será útil para apresentação do resultado final
		Integer idInstancia = 1;

		// para cada instância o arquivo de entrada é lido,
		// a lista de autores é ordenada a partir do maior autor
		// e o algoritmo guloso do truck factor é executado
		for (String instance : instances) {
			authors = new HashMap<String, List<String>>();
			files = new HashMap<String, List<String>>();
			readInputFile(instance);
			orderAuthors();
			greedyTruckFactor("instance-" + idInstancia + " (" + instance + ")");
			++idInstancia;
		}

		// guardando tempo de execução total do programa
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();

		// contruindo arquivo html e abrindo-o no navegador padrão do sistema
		showHtmlFile(timeElapsed);
	}

	/**
	 * Método que implementa o Algoritmo Guloso que calcula o valor de truck factor
	 **/
	private static void greedyTruckFactor(String instanceName) throws IOException {

		// guardando instante em que o método do algoritmo guloso inicia
		startGreedyTruckFactor = Instant.now();
		System.out.println("Calculating Truck Factor...");

		// lista de autores que foram retirados, ou seja, que entram no cálculo do truck
		// factor
		List<String> authorsTF = new ArrayList<String>();

		// variável que guarda a quantidade que representa a metade dos arquivos totais
		// do projeto
		Integer halfFiles = files.size() / 2;

		for (Map.Entry<String, List<String>> author : authors.entrySet()) {

			// adicionando autor à lista de truck factor
			authorsTF.add((String) author.getKey());

			// removendo arquivos orfaos
			removeFiles(author);

			// verificando se mais da metade dos arquivos são orfaos
			if (files.size() <= halfFiles) {
				// parando execução do algoritmo
				break;
			}
		}

		// guardando instante em que o algoritmo guloso termina
		finishGreedyTruckFactor = Instant.now();

		// adicioanndo resultado ao código html que será utilizado para exibição do
		// resultado final
		addResultToHtml(instanceName, authorsTF);
	}

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** Métodos auxiliares **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/** ------------------------------------------ **/

	/**
	 * Método responsável por carregar e organizar os dados do arquivo de entrada
	 **/
	private static void readInputFile(String inputFileName) throws FileNotFoundException {

		// guardando instante em que a leitura do arquivo foi iniciada
		startReadInputFile = Instant.now();
		System.out.println("Reading input file...");

		File intputFile = new File(inputFileName);
		Scanner scanner = new Scanner(intputFile);

		String fileName = null;
		List<String> authorsFromFile = new ArrayList<String>();
		Integer totalCommits = 0;
		List<Integer> commitsPerAuthor = new ArrayList<Integer>();

		// lendo cada linha do arquivo de entrada e carregando para as listas de autores
		// e de arquivos
		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			if (getFileName(line) != null) {

				fileName = getFileName(line);

				if (authorsFromFile.size() > 0) {

					setAuthor(authorsFromFile, commitsPerAuthor, totalCommits, fileName);
					authorsFromFile = new ArrayList<String>();
					commitsPerAuthor = new ArrayList<Integer>();
					totalCommits = 0;
				}

				continue;
			}

			authorsFromFile.add(line.substring(7));
			Integer commitThatAuthor = Integer.parseInt(line.substring(0, 7).trim());
			commitsPerAuthor.add(commitThatAuthor);
			totalCommits += commitThatAuthor;

		}

		// exibindo dados da instância carregada a partir da leitura do arquivos de
		// entrada
		System.out.println();
		System.out.println("Total Developers: " + authors.size());
		System.out.println("Total Files: " + files.size());
		System.out.println();

		// registrando instante final da leitura do arquivo de entrada para instância
		finishReadInputFile = Instant.now();
	}

	/**
	 * Método para ordenar a lista de autores, de forma que o autor de mais arquivos
	 * seja o primeiro da lista
	 **/
	private static void orderAuthors() {

		startOrder = Instant.now();

		System.out.println("Order list authors...");

		Comparator<Entry<String, List<String>>> valueComparator = (e1,
				e2) -> e1.getValue().size() > e2.getValue().size() ? -1 : e1.getValue().size() < e2.getValue().size() ? 1 : 0;

		Map<String, List<String>> sortedMap = authors.entrySet().stream().sorted(valueComparator)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		authors = sortedMap;

		finishOrder = Instant.now();
	}

	/**
	 * Método que remove os autores de arquivos e, ainda remove os arquivos que se
	 * tornaram 'órfãos'
	 **/
	private static void removeFiles(Entry<String, List<String>> author) {

		List<String> remove = new ArrayList<String>();
		List<String> removeAuthor = new ArrayList<String>();

		Iterator<Entry<String, List<String>>> it = files.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> file = it.next();

			// remove author from file
			for (int i = 0; i < file.getValue().size(); i++) {

				if (file.getValue().get(i).equals(author.getKey())) {
					removeAuthor.add(file.getValue().get(i));
				}
			}

			for (String index : removeAuthor) {
				file.getValue().remove(index);
			}

			// remove file with zero authors
			if (file.getValue().size() == 0) {
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
	private static void setAuthor(List<String> authorsFromFile, List<Integer> commitsPerAuthor, Integer totalCommits,
			String fileName) {

		Integer commitsAdded = 0;
		for (int i = 0; i < authorsFromFile.size(); i++) {
			authors.putIfAbsent(authorsFromFile.get(i), new ArrayList<String>());
			files.putIfAbsent(fileName, new ArrayList<String>());

			commitsAdded += commitsPerAuthor.get(i);

			if (i > 0) {
				Boolean ok = ((commitsAdded * 100) / totalCommits) < 90;

				if (ok) {
					authors.get(authorsFromFile.get(i)).add(fileName);
					files.get(fileName).add(authorsFromFile.get(i));
				}
			} else {

				authors.get(authorsFromFile.get(i)).add(fileName);
				files.get(fileName).add(authorsFromFile.get(i));
			}

		}
	}

	private static String getFileName(String line) {

		if (line.indexOf("FILE: ") == 0) {

			return line.substring(6);
		}

		return null;
	}

	/**
	 * Método que incrementa o resultado de cada instância ao arquivo index.html que
	 * exibe o resultado final
	 **/
	private static void addResultToHtml(String instance, List<String> authorsTF) throws IOException {

		long timeReadInputFile = Duration.between(startReadInputFile, finishReadInputFile).toMillis();
		long timeOrder = Duration.between(startOrder, finishOrder).toMillis();
		long timeGredyTruckFactor = Duration.between(startGreedyTruckFactor, finishGreedyTruckFactor).toMillis();

		String colorBarTruckFactor = "";
		for (String author : authorsTF) {
			colorBarTruckFactor += "\"rgba(255, 99, 132, 0.2)\",";
		}

		String authorsHtml = "";
		String dataHtml = "";
		for (Map.Entry<String, List<String>> author : authors.entrySet()) {
			authorsHtml += "\"" + author.getKey() + "\",";
			dataHtml += "\"" + author.getValue().size() + "\",";
		}

		String chartsHtml = "<div class=\"flex-container\"> <div> <canvas id=\"" + instance
				+ "\" width=\"1000\" height=\"300\"></canvas> </div>";
		String datailHtml = "<div class=\"detail\"> <p>Truck Factor: " + authorsTF.size() + "</p> <p>Total developers: "
				+ authors.size() + "</p> <p>Time Grredy Truck Factor: " + timeGredyTruckFactor + " ms</p> <p>Time Order: "
				+ timeOrder + " ms</p> <p>Time Read Instance: " + timeReadInputFile + " ms</p> </div> </div>";
		String chartsScript = " var ctx = document.getElementById(\"" + instance
				+ "\"); var myChart = new Chart(ctx, { type: \"bar\",data: { labels: [" + authorsHtml
				+ "], datasets: [{ label: \"Truck Factor for " + instance + "\", data: [" + dataHtml + "], backgroundColor: ["
				+ colorBarTruckFactor
				+ "], borderWidth: 1 }] }, options: { tooltips: { callbacks: { label: function(tooltipItem, data) { return \"Num files that author: \" + data[\"datasets\"][0][\"data\"][tooltipItem[\"index\"]]; } },}, responsive: false, scales: { xAxes: [{ ticks: { maxRotation: 90, minRotation: 80 } }], yAxes: [{ ticks: { beginAtZero: true } }] } } });";

		String divider = "<br><hr><br>";

		htmlContent += divider + chartsHtml + datailHtml + "<script>" + chartsScript + "</script>";
	}

	/**
	 * Método que constrói o arquivo index.html e abre o navegador com a resposta da
	 * excução
	 **/
	private static void showHtmlFile(long timeElapsed) throws IOException {
		String style = "<style> .flex-container { display: flex; flex-wrap: nowrap; justify-content: center;} .flex-container > div { width: 1000px; margin: 20px; text-align: center;} .detail {border: 1px solid #aaa; width:250px !important; height:190px;} body {text-align:center; min-width: 1300px; color: #555} </style>";
		String header = "<h3>Truck Factor - Matheus Silva Ferreira</h3> <p>Num instances: " + instances.size()
				+ "</p> <p>Total Time: " + timeElapsed + " ms</p>";
		String start = "<!DOCTYPE html> <html> <head> <meta charset=\"UTF-8\"> <title>Truck Factor</title> " + style
				+ " </head> <body> <script src=\"Chart.min.js\"></script> " + header;
		String end = "</body> </html>";

		String codeHtml = start + htmlContent + end;

		BufferedWriter writer = new BufferedWriter(new FileWriter("result/index.html"));
		writer.write(codeHtml);
		writer.close();

		File htmlFile = new File("result/index.html");
		Desktop.getDesktop().browse(htmlFile.toURI());
	}

	/**
	 * As instancias padrao foram obtidas pelos primeiros resultados do github para
	 * pesquisa do termo "vuejs". Os arquivos de entrada foram gerados com o comando
	 * "sh generateInputTF.sh <diretorio do projeto git clonado> > <nome do arquivo
	 * de dados a ser gerado (txt)> ". Para as instancias padrao os resultados
	 * obtidos com o algoritmo de proposto por Avelino são:
	 * "entradas/easy-vue.txt"(1 - tigerb) "entradas/muse-ui.txt"(1 - myronliu347)
	 * "entradas/vue-apollo.txt"(1 - Guillaume) "entradas/vue-awesome-swiper.txt"(2
	 * - surmon, unknown) "entradas/vue-quill-editor.txt"(1 - surmon)
	 * "entradas/vue-video-player.txt"(1 - surmon)
	 * 
	 */
}
