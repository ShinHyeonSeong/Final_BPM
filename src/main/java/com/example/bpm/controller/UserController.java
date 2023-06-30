package com.example.bpm.controller;

import com.example.bpm.dto.ProjectDto;
import com.example.bpm.dto.UserDto;
import com.example.bpm.service.ProjectSerivce;
import com.example.bpm.service.UserService;

import javax.persistence.*;
import javax.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@Slf4j
@ToString
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    @Autowired
    final private UserService userService;
    @Autowired
    final private ProjectSerivce projectSerivce;
    @Autowired
    HttpSession session;

    public UserDto getSessionUser() {
        UserDto currentUser = (UserDto) session.getAttribute("userInfo");
        return currentUser;
    }

    public ProjectDto getSessionProject() {
        ProjectDto currentProject = (ProjectDto) session.getAttribute("currentProject");
        return currentProject;
    }

    public Long getSessionAuth() {
        Long auth = (Long) session.getAttribute("auth");
        return auth;
    }

    public void checkAuth() {
        ProjectDto projectDto = getSessionProject();
        UserDto userDto = getSessionUser();
        Long auth = userService.checkRole(projectDto.getProjectId(), userDto.getUuid());
        session.setAttribute("auth", auth);
    }

    public ModelAndView modelAndView(String html){
        ModelAndView mav = new ModelAndView(html);
        return mav;
    }


    @GetMapping(value = "login")
    public ModelAndView login() {
        return modelAndView("login");
    }

    @GetMapping(value = "/join")
    public ModelAndView goSave() {
        return modelAndView("join");
    }

    @PostMapping("dologin")
    public ModelAndView login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session, Model model) {
        UserDto loginResult = userService.login(email, password);
        if (loginResult != null) {
            //세션에 로그인한 정보롤 담아줌 -> main 창에 적용되고 main에 이 세션을 이용할 수 있는 thyleaf가 적용되는 것이다.
            session.removeAttribute("userInfo");
            session.setAttribute("userInfo", loginResult);
            //로그인 성공 알림창 만들어줘야함
            log.info("로그인 성공 세션 정상 입력 (컨트롤러 작동)");
            return modelAndView("redirect:/project/projectManagerList");
        } else {
            model.addAttribute("message", "이메일 혹은 비밀번호가 일치하지 않습니다.");
            log.info("로그인 실패 세션 적용 실패 (컨트롤러 작동)");
            return modelAndView("login");
        }
    }

    @PostMapping("/dosave")
    public ModelAndView save(@RequestParam("email") String email,
                       @RequestParam("password") String password,
                       @RequestParam("username") String name, Model model) {
        UserDto findUser = userService.findByEmail(email);
        if (findUser == null) {
            UserDto NewUser = new UserDto(email, password, name);
            log.info("DTO 정상 값 입력 (컨트롤러)" + "/" + email + "/" + password + "/" + name);
            UserDto result = userService.save(NewUser);
            return modelAndView("login");
        } else {
            model.addAttribute("message", "이미 있는 이메일 입니다.");
            return modelAndView("join");
        }
    }


    @GetMapping("logout")
    public ModelAndView logout(HttpSession session) {
        //세션으로 로그아웃 처리
        session.invalidate();
        log.info("로그아웃 성공 세션 정상 작동(컨트롤러)");
        return modelAndView("redirect:/index");
    }


    //프로필로 가는 메서드 세션값을 활용해서 user의 정보를 찾아낸다
    @GetMapping("/account")
    public ModelAndView goAccount(HttpSession session, Model model) {
        UserDto sessionUser = getSessionUser();
        UserDto result = userService.findByUser(sessionUser.getUuid());
        model.addAttribute("user", result);
        return modelAndView("account");
//        if (result != null) {
//            model.addAttribute("user", sessionUser);
//            log.info("회원정보 찾기 성공 (컨트롤러 작동) detail 페이지로 이동");
//            return "user/detail";
//        } else {
//            log.info("서비스에서 유저를 찾지 못함 (컨트롤러 작동)");
//            return null;
//        }
    }

    @GetMapping("/accountUpdate")
    public ModelAndView goAccountChange(HttpSession session, Model model) {
        UserDto sessionUser = getSessionUser();
        UserDto result = userService.findByUser(sessionUser.getUuid());
        model.addAttribute("user", result);
        return modelAndView("accountUpdate");
    }

    //프로필에서 정보 변경 시 유저의 정보를 찾아오는 메서드

    //회원 정보 변경 시 메서드
    @PostMapping("/update")
    public ModelAndView update(@RequestParam("email") String email,
                         @RequestParam("userName") String name, HttpSession session) {
        UserDto sessionUser = getSessionUser();
        log.info("변경 전 정보 " + sessionUser.getEmail() + sessionUser.getName());
        UserDto updateDto = userService.update(sessionUser, email, name);
        log.info("변경 후 정보 " + updateDto.getEmail() + updateDto.getName());
        if (updateDto != null) {
            log.info("정상 업데이트 되었습니다 (컨트롤러 작동)");
//            session.removeAttribute("userInfo");
//            session.setAttribute("userInfo", updateDto);
            return modelAndView("redirect:/user/login");
        } else {
            log.info("업데이트 불가 (컨트롤러 작동)");
            return modelAndView("redirect:/user/accountUpdate");
        }
    }

    @GetMapping("/passwordChange")
    public ModelAndView goPasswordChange(Model model) {
        UserDto sessionUser = getSessionUser();
        UserDto result = userService.findByUser(sessionUser.getUuid());
        model.addAttribute("user", result);
        return modelAndView("passwordChange");

    }

    // 비밀번호 변경 메서드
    @PostMapping("/passwordUpdate")
    public ModelAndView passwordChange(@RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session) {
        UserDto sessionUser = getSessionUser();
        log.info("컨트롤러 호출 완료");
        log.info(sessionUser.getPassword(), password, email, newPassword, confirmPassword);
        int result = userService.changePassword(sessionUser, email, password, newPassword, confirmPassword);
        if (result == 0) {
            session.removeAttribute("userInfo");
            return modelAndView("login");
        } else return modelAndView("login");
    }

    //회원 탈퇴 메서드
    @GetMapping("/delete")
    public ModelAndView deleteById(HttpSession session) {
        UserDto userDto = getSessionUser();
        userService.deleteById(userDto.getUuid());
        session.invalidate();
        log.info("탈퇴되었습니다 (컨트롤러 작동)");
        return modelAndView("redirect:/index");
    }

    @GetMapping("/search")
    public ModelAndView searchMember() {
        return modelAndView("searchMember");
    }

    @PostMapping("/returnSearch")
    public ModelAndView search(@RequestParam("searchKeyword") String searchKeyword, Model model) {
        log.info("검색 키워드 : " + searchKeyword);
        List<UserDto> dtoList = userService.searchUser(searchKeyword);

        if (dtoList.isEmpty()) {
            log.info("검색 결과 없음");
            return modelAndView("redirect:/user/search");
        }
        model.addAttribute("searchUsers", dtoList);
        return modelAndView("searchMember");
    }
}
