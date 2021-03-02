package mestrado.matheus.teamtracker.domain.entity;
import javax.persistence.Entity;
import javax.persistence.Id;

import mestrado.matheus.teamtracker.domain.Explore;

@Entity
public class EntityExplore {

	 @Id
	 public String checkoutLocalRepositoryZoomPath;
	 
	 public Explore explore;
}
