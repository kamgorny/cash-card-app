package example.cashcardapp.repository;

import example.cashcardapp.model.CashCard;
import org.springframework.data.repository.CrudRepository;

public interface CashCardRepository extends CrudRepository<CashCard, Long>
{
}