package gummySylic.work.repository;

import gummySylic.work.modal.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {

    List<Prompt> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Prompt> findByUser_TelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);
}
