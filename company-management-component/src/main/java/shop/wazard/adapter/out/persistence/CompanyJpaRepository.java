package shop.wazard.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.wazard.entity.company.CompanyJpa;

@Repository
interface CompanyJpaRepository extends JpaRepository<CompanyJpa, Long> {
}
