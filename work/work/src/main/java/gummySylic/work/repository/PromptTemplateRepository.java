package gummySylic.work.repository;

import gummySylic.work.modal.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    Optional<PromptTemplate> findByName(String name);

    List<PromptTemplate> findByCategoryAndIsActiveTrue(String category);
}
