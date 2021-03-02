package mestrado.matheus.teamtracker.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import mestrado.matheus.teamtracker.domain.Project;

@Entity
public class EntityProject {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;
	
	@Column(name = "checkoutLocalRepository", nullable = false)
	public String checkoutLocalRepository;

	@Column(name = "ID", nullable = false)
	public Project project;
}
