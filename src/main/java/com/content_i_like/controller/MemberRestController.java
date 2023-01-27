package com.content_i_like.controller;

import com.content_i_like.domain.Response;
import com.content_i_like.domain.dto.member.*;
import com.content_i_like.service.MailService;
import com.content_i_like.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberRestController {
    private final MemberService memberService;
    private final MailService mailService;

    @PostMapping("/join")
    public Response<MemberJoinResponse> join(@RequestBody MemberJoinRequest request){
        MemberJoinResponse userJoinResponse = memberService.join(request);
        return Response.success(userJoinResponse);
    }

    @PostMapping("/login")
    public Response<MemberLoginResponse> login(@RequestBody MemberLoginRequest request){
        MemberLoginResponse response = memberService.login(request);
        return Response.success(response);
    }

    @PostMapping("/passwd/find_pw")
    public Response<String> findPw(@RequestBody MemberFindRequest request){
        MailDto mailDto = memberService.findPwByEmail(request);
        System.out.println("메일을 발송하겠습니다.");
        mailService.mailSend(mailDto);
        return Response.success("임시 비밀번호를 메일로 발송했습니다.");
    }

}