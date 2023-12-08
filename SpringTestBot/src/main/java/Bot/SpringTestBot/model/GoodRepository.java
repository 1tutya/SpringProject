package Bot.SpringTestBot.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodRepository extends CrudRepository<Good, Long> {
    Good findBygoodName(String goodName);
}
