package br.com.home.lab.softwaretesting.repository;

import br.com.home.lab.softwaretesting.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @Modifying
    @Query(value = "DELETE FROM lancamento l where l.user_id = :userId", nativeQuery = true)
    void removeAllByUser(@Param("userId") long userId);
}
