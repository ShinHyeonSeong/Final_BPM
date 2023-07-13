package com.example.bpm.controller;

import com.example.bpm.dto.*;
import com.example.bpm.repository.UserRepository;
import com.example.bpm.service.ProjectDetailSerivce;
import com.example.bpm.service.ProjectSerivce;
//import jakarta.servlet.http.HttpSession;
import com.example.bpm.service.UserService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@Slf4j
@Builder
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    final private ProjectSerivce projectSerivce;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectDetailSerivce projectDetailSerivce;

    HttpSession session;

    /* - - - - - 지역 함수 시작 - - - - - */

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
        log.info("유저 권한 확인중. 유저명 : " + userDto.getName());
        Long auth = userService.checkRole(projectDto.getProjectId(), userDto.getUuid());
        log.info("권한 검색 종료. 접근 권한 : " + auth);
        session.setAttribute("auth", auth);
    }

    public ModelAndView modelAndView(String html) {
        ModelAndView mav = new ModelAndView(html);
        return mav;
    }

    /* - - - - - 지역 함수 끝 - - - - - */

    // 관리자 권한 프로젝트 리스트 출력
    @GetMapping("/projectManagerList")
    public ModelAndView getProjectList(HttpSession session, Model model) {
        //세션에서 현재 로그인 되어있는 유저의 정보를 가져온다
        UserDto sessionUser = (UserDto) session.getAttribute("userInfo");
        //UUID를 활용하여 권한자 / 비권한자 프로젝트 리스트를 불러온다
        List<ProjectDto> ManagerToProjectList = projectSerivce.findManagerToProjectList(sessionUser.getUuid());
        model.addAttribute("user", sessionUser);
        model.addAttribute("managerList", ManagerToProjectList);

        List<ProjectRequestDto> requestDtos = projectSerivce.findAllToRequestProject(sessionUser.getUuid());
        if (requestDtos.isEmpty()) {
            model.addAttribute("request", false);
        } else model.addAttribute("request", true);
        // 초대 여부 검사 후 프론트에서 시각적 알림 효과 부여

//        List<ProjectRoleDto> ParticipantsToProjectList = projectSerivce.findParticipantsToProjectList(sessionUser.getUuid());
//        if (ManagerToProjectList.isEmpty()) {
//            if (ParticipantsToProjectList.isEmpty()) {
//                log.info("참여중인 리스트가 없음");
//            } else {
//                log.info("비권한자 리스트만 있음");
//            }
//        } else {
//            if (ParticipantsToProjectList.isEmpty()) {
//                log.info("권한자 리스트만 있음");
//            } else {
//                log.info("둘다 리스트 있음");
//            }
//        }
//        //null값이던 데이터가 존재하던 어쨋든 리스트 창을 보여줘야한다. (빈 공백이라도)
//        model.addAttribute("ListToM", ManagerToProjectList);
//        model.addAttribute("ListToP", ParticipantsToProjectList);
        return modelAndView("projectManagerList");
    }

    // 프로젝트 멤버 권한 리스트 출력
    @GetMapping("/projectMemberList")
    public ModelAndView projectMemberList(HttpSession session, Model model) {
        //세션에서 현재 로그인 되어있는 유저의 정보를 가져온다
        UserDto sessionUser = getSessionUser();
        //UUID를 활용하여 권한자 / 비권한자 프로젝트 리스트를 불러온다
        List<ProjectDto> memberToProjectList = projectSerivce.findParticipantsToProjectList(sessionUser.getUuid());
        model.addAttribute("user", sessionUser);
        model.addAttribute("memberList", memberToProjectList);

        List<ProjectRequestDto> requestDtos = projectSerivce.findAllToRequestProject(sessionUser.getUuid());
        if (requestDtos.isEmpty()) {
            model.addAttribute("request", false);
        } else model.addAttribute("request", true);
        return modelAndView("projectMemberList");
    }

    // 전체 프로젝트 리스트 출력
    @GetMapping("/projectAllList")
    public ModelAndView projectAllList(Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("userInfo");
        //UUID를 활용하여 권한자 / 비권한자 프로젝트 리스트를 불러온다
        List<ProjectDto> ManagerToProjectList = projectSerivce.findManagerToProjectList(sessionUser.getUuid());
        model.addAttribute("user", sessionUser);
        List<ProjectDto> AllProjectList = projectSerivce.findAllToProjectList();
        model.addAttribute("projectAllList", AllProjectList);
        List<ProjectRequestDto> requestDtos = projectSerivce.findAllToRequestProject(sessionUser.getUuid());
        if (requestDtos.isEmpty()) {
            model.addAttribute("request", false);
        } else model.addAttribute("request", true);
        return modelAndView("projectAllList");
    }


    @GetMapping("/lunch")
    public ModelAndView lunchProject() {
        return modelAndView("projectLunch");
    }

    @GetMapping("/create")
    public ModelAndView projectCreate(Model model) {
        UserDto nowUser = getSessionUser();
        model.addAttribute("nowUser", nowUser);
        return modelAndView("projectCreate");
    }

    @PostMapping("/createPage")
    public ModelAndView createProject(@RequestParam(value = "title") String title,
                                      @RequestParam(value = "subtitle") String subtitle,
                                      @RequestParam(value = "startDay") String startDay,
                                      @RequestParam(value = "endDay") String endDay,
                                      HttpSession session) {
        if (title.equals(null) || startDay.equals(null) || endDay.equals(null)) {
            log.info("값을 다 입력하지 못했음 (컨트롤러 작동)");
            return modelAndView("projectCreate");
        } else {
            ProjectDto projectDto = projectSerivce.createProject(title, subtitle, startDay, endDay);
            log.info(projectDto.getProjectId().toString());
            UserDto sessionUser = getSessionUser();
            session.setAttribute("currentProject", projectDto);
            projectSerivce.autorization(projectDto, sessionUser);
            log.info("프로젝트 생성 정상 작동(컨트롤러 작동)");
            return modelAndView("redirect:/project/projectManagerList");
        }
    }

    // 프로젝트 선택 시 그 프로젝트 정보를 가져오며 프로젝트 창으로 넘어가는 메서드
    // 권한 검색 및 부여 후, 권한에 따라 페이지 리턴.
    @RequestMapping("/{id}")
    public ModelAndView selectProject(@PathVariable("id") Long id, HttpSession session, Model model) {
        UserDto userDto = getSessionUser();
        ProjectDto presentDto = projectSerivce.selectProject(id);
        List<UserDto> userDtoList = userService.searchUserToProject(id);
        List<HeadDto> headDtoList = projectDetailSerivce.selectAllHead(presentDto);

        session.removeAttribute("currentProject");
        session.setAttribute("currentProject", presentDto);
        checkAuth();
        Long auth = getSessionAuth();

        model.addAttribute("auth", auth);

        model.addAttribute("projectDto", presentDto);
        model.addAttribute("joinUsers", userDtoList);
        model.addAttribute("headDtoList", headDtoList);

        if (getSessionAuth() != 2) {
            List<WorkDto> userWorkDtoList = projectDetailSerivce.selectAllWorkForProject(presentDto);

            model.addAttribute("userWorkDtoList", userWorkDtoList);
            return modelAndView("projectMain");
        } else if (getSessionAuth() == 2) {
            List<DetailDto> detailDtoList = projectDetailSerivce.selectAllDetailForProject(projectSerivce.selectProject(id));
            List<WorkDto> workDtoList = projectDetailSerivce.selectAllWorkForProject(presentDto);
            List<DocumentDto> documentDtoList = projectDetailSerivce.selectAllDocumentForWorkList(workDtoList);

            model.addAttribute("detailDtoList", detailDtoList);
            model.addAttribute("workDtoList", workDtoList);
            model.addAttribute("documentDtoList", documentDtoList);

            return modelAndView("onlyReadPage");
        }
        //권한이 없습니다 알람창 띄우기
        return null;
    }

    // 프로젝트 초대 확인창
    @GetMapping("/inviteList")
    public ModelAndView inviteList(HttpSession session, Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("userInfo");
        //UUID를 활용하여 권한자 / 비권한자 프로젝트 리스트를 불러온다
        model.addAttribute("user", sessionUser);

        List<ProjectRequestDto> requestDtos = projectSerivce.findAllToRequestProject(sessionUser.getUuid());
        if (requestDtos.isEmpty()) {
            model.addAttribute("request", false);
        } else {
            model.addAttribute("request", true);
            model.addAttribute("requestList", requestDtos);
        }
        return modelAndView("inviteList");
    }

    // 프로젝트 초대 발송 컨트롤러
    @RequestMapping("/invite/{id}")
    public ModelAndView sendInvite(@PathVariable("id") String uuid, HttpSession session, Model model) {
        UserDto sendUser = (UserDto) session.getAttribute("userInfo");
        UserDto recvUser = UserDto.toUserDto(userRepository.findById(uuid).orElse(null));
        ProjectDto projectDto = (ProjectDto) session.getAttribute("currentProject");
        projectSerivce.sendInvite(sendUser.getUuid(), recvUser.getUuid(), projectDto.getProjectId());
        return modelAndView("redirect:/user/search");
    }

    // 프로젝트 초대 수락, 거절 컨트롤러
    // @PathVariable 통해 전달하여 url 노출됨. 추후 재고
    @RequestMapping("/requestResponse/{sendUser}/{recvUser}/{project}/{acceptable}")
    public ModelAndView requestResponse(@PathVariable("sendUser") String sendUuid,
                                  @PathVariable("recvUser") String recvUuid,
                                  @PathVariable("project") Long projectId,
                                  @PathVariable("acceptable") boolean acceptable) {
        projectSerivce.submitInvite(sendUuid, recvUuid, projectId, acceptable);
        return modelAndView("redirect:/project/inviteList");
    }



}

