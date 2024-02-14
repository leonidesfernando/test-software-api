package br.com.home.lab.softwaretesting.repository;

import br.com.home.lab.softwaretesting.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @Modifying
    @Query(value = "TRUNCATE TABLE lancamento", nativeQuery = true)
    void truncateTable();
}
