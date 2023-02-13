package com.content_i_like.controller;

import com.content_i_like.domain.Response;
import com.content_i_like.domain.dto.SortStrategy;
import com.content_i_like.domain.dto.search.SearchMembersResponse;
import com.content_i_like.domain.dto.search.SearchPageGetResponse;
import com.content_i_like.domain.dto.search.SearchRecommendsResponse;
import com.content_i_like.domain.dto.search.SearchRequest;
import com.content_i_like.domain.dto.tracks.TrackGetResponse;
import com.content_i_like.service.CacheService;
import com.content_i_like.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

  private final SearchService searchService;
  private final CacheService cacheService;

  @GetMapping()
  public String searchMainPage(HttpServletRequest httpRequest, Model model) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }

    SearchRequest searchKeyword = SearchRequest.builder().build();
    model.addAttribute("keywordDto", searchKeyword);

    SortStrategy sortStrategy = SortStrategy.builder().build();
    model.addAttribute("sortStrategy", sortStrategy);

    List<String> allProperties = List.of("createdAt", "trackTitle");
    model.addAttribute("allProperties", allProperties);

    return "pages/search/search-main";
  }

  @GetMapping("/tracks")
  public String searchTracksByKeyword(HttpServletRequest httpRequest,
      @ModelAttribute("keywordDto") final SearchRequest trackTitle,
      @PageableDefault(size=8, sort="trackTitle", direction= Direction.DESC) Pageable pageable,
      @RequestParam(value="page", required = false) Integer pageNum,
      Model model) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }

    SearchPageGetResponse<TrackGetResponse> trackResults =
        searchService.findTracksWithKeyword(pageable, trackTitle.getKeyword(), "sjeon0730@gmail.com");

    model.addAttribute("trackResults", trackResults);
    model.addAttribute("trackResultsAsList", trackResults.getPages().toList());
    model.addAttribute("pageable", pageable);
    model.addAttribute("keyword", trackTitle.getKeyword());

    String newLineChar = System.getProperty("line.separator").toString();
    model.addAttribute("newline", newLineChar);

    return "pages/search/tracks-search";
  }

  @GetMapping("/members")
  public String searchMembersByKeyword(HttpServletRequest httpRequest,
      @ModelAttribute("keywordDto") final SearchRequest nickName,
      @PageableDefault(sort="createdAt", direction= Direction.DESC) Pageable pageable,
      @RequestParam(value="page", required = false) Integer pageNum,
      Model model) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }

    SearchPageGetResponse<SearchMembersResponse> searchedMembers =
        searchService.findMembersWithKeyword(pageable, nickName.getKeyword(), "sjeon0730@gmail.com");

    model.addAttribute("keyword", nickName.getKeyword());
    model.addAttribute("memberNickName", searchedMembers);
    model.addAttribute("memberNickNameAsList", searchedMembers.getPages().toList());
    model.addAttribute("pageable", pageable);

    return "pages/search/members-search";
  }

  @GetMapping("/recommends")
  public String searchRecommendsByKeyword(HttpServletRequest httpRequest,
      @ModelAttribute("keywordDto") final SearchRequest recommendTitle,
      @RequestParam(value="page", required = false) Integer pageNum,
      @PageableDefault(size=5, sort="createdAt", direction = Direction.DESC) Pageable pageable,
      Model model) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }

    SearchPageGetResponse<SearchRecommendsResponse> pagedResponseRecommends =
        searchService.findRecommendsWithKeyword(pageable, recommendTitle.getKeyword(), "sjeon0730@gmail.com");

    model.addAttribute("recommendsList", pagedResponseRecommends);
    model.addAttribute("recommendsListAsList", pagedResponseRecommends.getPages().toList());
    model.addAttribute("keyword", recommendTitle.getKeyword());
    model.addAttribute("pageable", pageable);

    return "pages/search/recommends-search";
  }

  @GetMapping("/all")
  public String searchAll(HttpServletRequest httpRequest,
      @ModelAttribute("keywordDto") final SearchRequest searchKeyword,
//      @ModelAttribute("sortStrategy") final SortStrategy sort,
      @PageableDefault(size=2, direction=Direction.DESC) Pageable pageable,
      Model model) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }

//    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort.getProperty()));

    SearchPageGetResponse<TrackGetResponse> trackResults =
        searchService.findTracksWithKeyword(pageable, searchKeyword.getKeyword(), "sjeon0730@gmail.com");

    SearchPageGetResponse<SearchMembersResponse> searchedMembers =
        searchService.findMembersWithKeyword(pageable, searchKeyword.getKeyword(), "sjeon0730@gmail.com");

    SearchPageGetResponse<SearchRecommendsResponse> pagedResponseRecommends =
        searchService.findRecommendsWithKeyword(pageable, searchKeyword.getKeyword(), "sjeon0730@gmail.com");

    model.addAttribute("tracks", trackResults);
    model.addAttribute("tracksAsList", trackResults.getPages().toList());
    model.addAttribute("members", searchedMembers);
    model.addAttribute("membersAsList", searchedMembers.getPages().toList());
    model.addAttribute("recommends", pagedResponseRecommends);
    model.addAttribute("keyword", searchKeyword.getKeyword());
    model.addAttribute("recommendsAsList", pagedResponseRecommends.getPages().toList());

    return "pages/search/search-all";
  }
}