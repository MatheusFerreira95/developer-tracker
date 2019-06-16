
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

	static Map<String, List<String>> authors = new HashMap<String, List<String>>();
	static Map<String, List<String>> files = new HashMap<String, List<String>>();
	static List<String> instances = new ArrayList<String>(Arrays.asList("input.txt", "input2.txt"));
	static String htmlContent = "";
	static Instant startReadInputFile;
	static Instant finishReadInputFile;
	static Instant startOrder;
	static Instant finishOrder;
	static Instant startGreedyTruckFactor;
	static Instant finishGreedyTruckFactor;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		Instant start = Instant.now();

		String inputFileName = args.length > 0 ? args[0] : null;

		if (inputFileName != null) {

			instances = new ArrayList<String>(Arrays.asList(inputFileName));
		}

		Integer index = 1;

		for (String instance : instances) {

			authors = new HashMap<String, List<String>>();
			files = new HashMap<String, List<String>>();

			readInputFile(instance);
			orderAuthors();
			greedyTruckFactor("instance-" + index);
			++index;
		}

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();

		showHtmlFile(timeElapsed);
	}

	/**
	 * Método responsável por carregar e organizar os dados do arquivo de entrada
	 **/
	private static void readInputFile(String inputFileName) throws FileNotFoundException {

		startReadInputFile = Instant.now();

		System.out.println("Reading input file...");

		File intputFile = new File(inputFileName);

		Scanner scanner = new Scanner(intputFile);

		String fileName = null;

		// lendo cada linha do arquivo de entrada e carregando para as listas de autores
		// e de arquivos
		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			if (getFileName(line) != null) {

				fileName = getFileName(line);
				continue;
			}

			String authorName = line;

			setAuthor(authorName, fileName);
		}

		System.out.println();
		System.out.println("Total Developers: " + authors.size());
		System.out.println("Total Files: " + files.size());
		System.out.println();

		finishReadInputFile = Instant.now();
	}

	/**
	 * Método que implementa o Algoritmo Guloso que calcula o valor de truck factor
	 **/
	private static void greedyTruckFactor(String inputFileName) throws IOException {

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

			// removendo autoria do autor dos arquivos do projeto e removendo arquivos
			// orfaos
			removeFiles(author);

			// verificando se mais da metade dos arquivos são orfaos
			if (files.size() < halfFiles) {

				// parando execução do algoritmo
				break;
			}
		}

		finishGreedyTruckFactor = Instant.now();

		// adicioanndo ao resultado final em html
		addResultToHtml(inputFileName, authorsTF);
	}

	/** ------------------------------------------ **/
	/** Métodos auxiliares **/
	/** ------------------------------------------ **/

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

	/**
	 * Método que Edita o arquivo index.html da saída
	 **/
	private static void addResultToHtml(String instance, List<String> authorsTF) throws IOException {

		long timeReadInputFile = Duration.between(startReadInputFile, finishReadInputFile).toMillis();
		long timeOrder = Duration.between(startOrder, finishOrder).toMillis();
		long timeGredyTruckFactor = Duration.between(startGreedyTruckFactor, finishGreedyTruckFactor).toMillis();

		String colorBarTruckFactor = "";
		for (String author : authorsTF) {
			colorBarTruckFactor += "'rgba(255, 99, 132, 0.2)',";
		}

		String authorsHtml = "";
		String dataHtml = "";
		for (Map.Entry<String, List<String>> author : authors.entrySet()) {
			authorsHtml += "'" + author.getKey() + "',";
			dataHtml += "'" + author.getValue().size() + "',";
		}

		String chartsHtml = "<div class='flex-container'> <div> <canvas id='" + instance
				+ "' width='500' height='300'></canvas> </div>";
		String datailHtml = "<div class='detail'> <p>Truck Factor: " + authorsTF.size() + "</p> <p>Time Grredy Truck Factor: "
				+ timeGredyTruckFactor + " ms</p> <p>Time Order: " + timeOrder + " ms</p> <p>Time Read Instance: "
				+ timeReadInputFile + " ms</p> </div> </div>";
		String chartsScript = " var ctx = document.getElementById('" + instance
				+ "'); var myChart = new Chart(ctx, { type: 'bar',data: { labels: [" + authorsHtml
				+ "], datasets: [{ label: 'Truck Factor for " + instance + "', data: [" + dataHtml + "], backgroundColor: ["
				+ colorBarTruckFactor
				+ "], borderWidth: 1 }] }, options: { tooltips: { callbacks: { label: function(tooltipItem, data) { return 'Num files that author: ' + data['datasets'][0]['data'][tooltipItem['index']]; } },}, responsive: false, scales: { xAxes: [{ ticks: { maxRotation: 90, minRotation: 80 } }], yAxes: [{ ticks: { beginAtZero: true } }] } } });";

		String divider = "<br><hr><br>";

		htmlContent += divider + chartsHtml + datailHtml + "<script>" + chartsScript + "</script>";
	}

	/**
	 * Método que Edita o arquivo index.html e abre o navegador com a resposta da
	 * excução
	 **/
	private static void showHtmlFile(long timeElapsed) throws IOException {
		String style = "<style> .flex-container { display: flex; flex-wrap: nowrap; justify-content: center;} .flex-container > div { width: 500px; margin: 50px; text-align: center;} .detail {border: 1px solid #aaa; width:300px !important; height:151px;} body {text-align:center; min-width: 900px; color: #555} </style>";
		String header = "<h3>Truck Factor - Matheus Silva Ferreira</h3> <p>Num instances: " + instances.size()
				+ "</p> <p>Total Time: " + timeElapsed + " ms</p>";
		String start = "<!DOCTYPE html> <html> <head> <meta charset='UTF-8'> <title>Truck Factor</title> " + style
				+ " </head> <body> <script src='Chart.min.js'></script> " + header;
		String end = "</body> </html>";

		String codeHtml = start + htmlContent + end;

		BufferedWriter writer = new BufferedWriter(new FileWriter("index.html"));
		writer.write(codeHtml);
		writer.close();

		File htmlFile = new File("index.html");
		Desktop.getDesktop().browse(htmlFile.toURI());
	}
}