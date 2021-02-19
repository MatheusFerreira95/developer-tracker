package mestrado.matheus.teamtracker;

import mestrado.matheus.teamtracker.util.Git;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TeamtrackerApplication {

	public static void main(String[] args) {

		Git.getLocalRepositoryFromLocalProject();
		SpringApplication.run(TeamtrackerApplication.class, args);
	}

}
