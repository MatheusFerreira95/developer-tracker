package mestrado.matheus.teamtracker.domain.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import mestrado.matheus.teamtracker.domain.Project;

public interface EntityProjectRepository extends JpaRepository<Project, String> {
}
