package com.content_i_like.controller;

import com.content_i_like.domain.dto.follow.FollowMyFeedResponse;
import com.content_i_like.domain.dto.member.ChangePwRequest;
import com.content_i_like.domain.dto.member.MemberCommentResponse;
import com.content_i_like.domain.dto.member.MemberFindRequest;
import com.content_i_like.domain.dto.member.MemberJoinRequest;
import com.content_i_like.domain.dto.member.MemberLoginRequest;
import com.content_i_like.domain.dto.member.MemberLoginResponse;
import com.content_i_like.domain.dto.member.MemberModifyRequest;
import com.content_i_like.domain.dto.member.MemberPointResponse;
import com.content_i_like.domain.dto.member.MemberRecommendResponse;
import com.content_i_like.domain.dto.member.MemberResponse;
import com.content_i_like.domain.dto.notification.NotificationThymeleafResponse;
import com.content_i_like.domain.enums.GenderEnum;
import com.content_i_like.exception.ContentILikeAppException;
import com.content_i_like.service.MemberService;
import com.content_i_like.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

  private final MemberService memberService;
  private final NotificationService notificationService;

  @ModelAttribute("genderEnums")
  public GenderEnum[] genderEnum() {
    return GenderEnum.values();
  }

  @GetMapping("/login")
  public String loginForm(HttpServletRequest request, Model model, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (checkSession(request, response)) {
      return "redirect:/";
    }
    String referrer = request.getHeader("Referer");

    model.addAttribute("request", new MemberLoginRequest());
    model.addAttribute("referrer", referrer);
    return "pages/member/login";
  }

  private boolean checkSession(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      MemberLoginResponse memberLoginResponse = (MemberLoginResponse) session
          .getAttribute("loginUser");
      if (memberLoginResponse != null) {
        return true;
      }
      new SecurityContextLogoutHandler()
          .logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }
    return false;
  }

  @PostMapping("/login")
  public String login(
      @Valid @ModelAttribute("memberLoginRequest") MemberLoginRequest memberLoginRequest,
      BindingResult bindingResult,
      HttpServletRequest request, Pageable pageable,
      @RequestParam(required = false) String redirect) {

    if (bindingResult.hasErrors()) {
      return "redirect:/member/login";
    }
    try {
      MemberLoginResponse response = memberService.login(memberLoginRequest);
      List<NotificationThymeleafResponse> notificationsResponses = notificationService
          .getNotificationsThymeleafResponses(
              response.getNickName(), pageable);

      HttpSession session = request.getSession();
      session.setAttribute("loginUser", response);
      session.setAttribute("notification", notificationsResponses);
      log.info("로그인 완료");
    } catch (ContentILikeAppException e) {
      log.info("에러 발생");
      return "redirect:/member/login";
    }
    if (redirect == null) {
      return "redirect:/";
    } else {
      return "redirect:" + redirect;
    }
  }

  @GetMapping("/logout")
  public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
    new SecurityContextLogoutHandler()
        .logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    return "redirect:/";
  }

  @GetMapping("/join")
  public String joinForm(HttpServletRequest request, Model model, HttpServletResponse response) {
    if (checkSession(request, response)) {
      return "redirect:/";
    }

    model.addAttribute("request", new MemberJoinRequest());
    return "pages/member/join";
  }

  @PostMapping("/join")
  public String join(@ModelAttribute("memberJoinRequest") MemberJoinRequest request) {
    memberService.join(request);
    return "redirect:/member/login";
  }

  @GetMapping("/passwd/find_pw")
  public String findPwForm(HttpServletRequest httpRequest, Model model,
      HttpServletResponse response) {
    HttpSession session = httpRequest.getSession(false);
    if (checkSession(httpRequest, response)) {
      return "redirect:/";
    }
    model.addAttribute("request", new MemberFindRequest());
    return "pages/member/find-pw";
  }

  @PostMapping("/passwd/find_pw")
  public String findPw(@Valid @ModelAttribute("memberFindRequest") MemberFindRequest request) {
    memberService.findPwByEmail(request);
    return "redirect:/member/login";
  }

  @GetMapping("/passwd/change")
  public String changePw(HttpServletRequest httpRequest, Model model,
      HttpServletResponse response) {
    HttpSession session = httpRequest.getSession(false);
    if (checkSession(httpRequest, response)) {
      return "redirect:/";
    }
    model.addAttribute("request", new ChangePwRequest());
    return "pages/member/change-pw";
  }

  @PostMapping("/passwd/change")
  public String changePw(HttpServletRequest httpRequest,
      @Valid @ModelAttribute("changePwRequest") ChangePwRequest request) {
    HttpSession session = httpRequest.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");
    log.info("세션 정보: {} and {}", loginResponse.getMemberNo(), loginResponse.getNickName());
    memberService.changePwByEmail(loginResponse, request);
    return "redirect:/";
  }

  @GetMapping("/my")
  public String getMyInfo(HttpServletRequest httpRequest, Model model) {
    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");

    model.addAttribute("member", memberService.getMyInfoByLoginResponse(loginResponse));
    return "pages/member/my-profile";
  }

  @GetMapping("/my/update")
  public String modifyMyInfo(HttpServletRequest request, Model model) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");

    model.addAttribute("member", memberService.getMyInfoByLoginResponse(loginResponse));
    model.addAttribute("request", new MemberModifyRequest());
    return "pages/member/modify-profile";
  }

  @PostMapping("/my/update")
  public String modifyMyInfo(
      @ModelAttribute("request") @Valid final MemberModifyRequest request,
      HttpServletRequest servletRequest) throws IOException {
    HttpSession session = servletRequest.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");

    MemberResponse memberResponse = memberService
        .modifyMyInfoWithFile(request, loginResponse);

    MemberLoginResponse updateLoginResponse = new MemberLoginResponse(loginResponse.getJwt(),
        loginResponse.getMemberNo(), loginResponse.getNickName(),
        memberResponse.getProfileImgUrl());
    session.setAttribute("loginUser", updateLoginResponse);
    return "redirect:/member/my";
  }

  @GetMapping("/my/point")
  public String getMyPoint(HttpServletRequest request, Model model, Pageable pageable) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return "redirect:/member/login";
    }
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");

    MemberPointResponse memberPointResponse = memberService
        .getMyPointByLoginResponse(loginResponse, pageable);
    model.addAttribute("response", memberPointResponse);
    return "pages/member/point-history";
  }


  @GetMapping("/recommends")
  public String getMyRecommends(HttpServletRequest request, Model model, Pageable pageable) {

    HttpSession session = request.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");
    MemberRecommendResponse recommends = memberService.getMyRecommendsByNickName(
        loginResponse.getNickName(), pageable);

    model.addAttribute("recommendsResponse", recommends);
    model.addAttribute("url", "");

    return "pages/member/myFeed-recommends";
  }

  @GetMapping("/comments")
  public String getMyComments(HttpServletRequest request, Model model, Pageable pageable) {
    HttpSession session = request.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");
    MemberCommentResponse comments = memberService.getMyCommentsByNickName(
        loginResponse.getNickName(), pageable);

    model.addAttribute("comments", comments);
    model.addAttribute("url", "");

    return "pages/member/myFeed-comments";
  }

  @GetMapping("/followers")
  public String getMyFollowers(HttpServletRequest request, Model model, Pageable pageable) {
    HttpSession session = request.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");
    FollowMyFeedResponse myFollowersByNickName = memberService.getMyFollowersByNickName(
        loginResponse.getNickName(), pageable);

    model.addAttribute("followers", myFollowersByNickName);
    model.addAttribute("url", "");

    return "pages/member/myFeed-followers";
  }

  @GetMapping("/followings")
  public String getMyFollowings(HttpServletRequest request, Model model, Pageable pageable) {
    HttpSession session = request.getSession(false);
    MemberLoginResponse loginResponse = (MemberLoginResponse) session.getAttribute("loginUser");
    FollowMyFeedResponse myFollowersByNickName = memberService.getMyFollowingsByNickName(
        loginResponse.getNickName(), pageable);

    model.addAttribute("followers", myFollowersByNickName);
    model.addAttribute("url", "");

    return "pages/member/myFeed-followings";
  }


  //ajax 중복체크
  @PostMapping("/nickNameChk")
  public void memberChk(HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {
    String memberNickName = request.getParameter("nickName");
    boolean result = memberService.checkMemberNickName(memberNickName);
    JSONObject jso = new JSONObject();
    jso.put("result", result);

    response.setContentType("text/html;charset=utf-8");
    PrintWriter out = response.getWriter();
    out.print(jso.toString());
  }

  @PostMapping("/emailChk")
  public void emailChk(HttpServletRequest request, HttpServletResponse response, Model model)
      throws IOException {
    String memberEmail = request.getParameter("email");
    boolean result = memberService.checkMemberEmail(memberEmail);
    JSONObject jso = new JSONObject();
    jso.put("result", result);

    response.setContentType("text/html;charset=utf-8");
    PrintWriter out = response.getWriter();
    out.print(jso.toString());
  }

  //ajax 로그인 체크
  @PostMapping("/loginCheck")
  public void loginCheck(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String memberEmail = request.getParameter("email");
    String memberPw = request.getParameter("password");
    MemberLoginRequest memberLoginRequest = new MemberLoginRequest(memberEmail, memberPw);
    boolean result = memberService.checkLogin(memberLoginRequest);
    JSONObject jso = new JSONObject();
    jso.put("result", result);
    response.setContentType("text/html;charset=utf-8");
    PrintWriter out = response.getWriter();
    out.print(jso.toString());

  }
}