package com.kairui.spider.repository.internet;

import com.kairui.spider.domain.internet.Domains;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainsRepository extends PagingAndSortingRepository<Domains, String> {

    List<Domains> findByDomain(String domain);

    Page<Domains> findByDomain(Pageable page,String domain);

    Page<Domains> findByIdNotNull(Pageable page);
}
