package mestrado.matheus.teamtracker.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de domínio que representa os dados de exploração do projeto.
 * Focado apenas em armazenar os dados para transporte (POJO).
 */
public class Explore {
	public final static String START_READ = "developer-tracker-start-read-info-file";
	public final static String STOP_READ = "developer-tracker-stop-read-info-file";

	public List<NodeExplore> nodeList = new ArrayList<>();
	public List<LinkExplore> linkList = new ArrayList<>();
}
