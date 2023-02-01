package com.content_i_like.repository;

import com.content_i_like.domain.entity.Member;
import com.content_i_like.domain.entity.Recommend;
import com.content_i_like.repository.custom.CustomRecommendRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendRepository extends JpaRepository<Recommend, Long>,
    CustomRecommendRepository {

  @Override
  @Modifying
  <S extends Recommend> S save(S entity);

  @Modifying(clearAutomatically = true)
  @Query("update Recommend r set r.recommendTitle = :title, r.recommendContent = :content, r.recommendImageUrl = :image, r.recommendYoutubeUrl = :youtube  ,r.lastModifiedAt = current_timestamp where r.recommendNo = :recommendNo ")
  void update(@Param("title") String recommendTitle, @Param("content") String recommendContent,
      @Param("image") String recommendImageUrl, @Param("youtube") String recommendYoutubeUrl,
      @Param("recommendNo") Long recommendNo);

  Page<Recommend> findAllByMember(Pageable pageable, Member member);
}
