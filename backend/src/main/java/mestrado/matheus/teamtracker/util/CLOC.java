package mestrado.matheus.teamtracker.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import mestrado.matheus.teamtracker.domain.NumLocProgrammingLanguage;
import mestrado.matheus.teamtracker.domain.Project;

public class CLOC {

	public static final String LIST_LANG_EXLUDED = "XML,Markdown,JSON,Maven,YAML,SVG,Bourne Shell,DOS Batch";

	// Requer https://github.com/AlDanial/cloc/blob/master/cloc instalada
	public static void buildNumLocProgrammingLanguageList(Project project) throws IOException, InterruptedException {
		
		String pathApp = new File(".").getCanonicalPath();
		String pathCLOC = pathApp.equals("/") ? pathApp + "cloc-1.86.pl" : pathApp + "/backend/cloc-tool/cloc-1.86.pl"; // tratando para imagens docker (ver cópia realizada no arquivo dockerfile)
		
		GitOutput gitOutput = Git.runCommand(project,
				"perl " + pathCLOC + " ./ --json --exclude-lang=\"" + CLOC.LIST_LANG_EXLUDED + "\" --out cloc-out.txt");

		File outpupFile = new File(project.localRepository + "/cloc-out.txt");

		Boolean outputFileCreated = false;
		for (String line : gitOutput.outputList) {
			if (line.contains("cloc-out.txt")) {
				outputFileCreated = true;
				break;
			}
		}

		if (!outputFileCreated)
			return;

		while (!outpupFile.exists()) {

			System.out.println("loading...");
		}

		Scanner scanner = new Scanner(outpupFile);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			String nameProgrammingLanguage = line.substring(1, line.lastIndexOf("\""));
			if (!line.contains("{") || line.contains("header"))
				continue;

			String lastCaracter = ",";
			if (!nameProgrammingLanguage.contentEquals("SUM")) {

				lastCaracter = "}";
				line = scanner.nextLine();

			}

			line = scanner.nextLine();
			String blank = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(","));

			line = scanner.nextLine();
			String comment = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(","));

			line = scanner.nextLine();
			String code = line.substring(line.lastIndexOf(" ") + 1, line.indexOf(lastCaracter));

			Integer sumLOC = Integer.parseInt(blank) + Integer.parseInt(comment) + Integer.parseInt(code);

			project.numLocProgrammingLanguageList.add(new NumLocProgrammingLanguage(nameProgrammingLanguage, sumLOC));
		}

		Integer sumTotal = project.numLocProgrammingLanguageList
				.get(project.numLocProgrammingLanguageList.size() - 1).percentLOC;
		project.numLocProgrammingLanguageList.remove(project.numLocProgrammingLanguageList.size() - 1);

		Integer hightTotal = 0;
		Integer index = 0;

		// recalculando porcentagem e excluindo os insignificantes
		for (NumLocProgrammingLanguage numLocProgrammingLanguage : project.numLocProgrammingLanguageList) {

			index++;

			if (hightTotal < 93) {

				numLocProgrammingLanguage.percentLOC = numLocProgrammingLanguage.percentLOC * 100 / sumTotal;
				hightTotal += numLocProgrammingLanguage.percentLOC;

			} else {

				project.numLocProgrammingLanguageList.add(index - 1,
						new NumLocProgrammingLanguage("Others", 100 - hightTotal));
				project.numLocProgrammingLanguageList = project.numLocProgrammingLanguageList.subList(0, index);
				break;
			}

		}

	}
}
