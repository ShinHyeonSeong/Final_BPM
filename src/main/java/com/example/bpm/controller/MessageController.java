package com.example.bpm.controller;

import com.example.bpm.dto.MessageDto;
import com.example.bpm.dto.ProjectDto;
import com.example.bpm.dto.UserDto;
import com.example.bpm.service.MessageService;
import com.example.bpm.service.ProjectDetailSerivce;
import com.example.bpm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@Slf4j
@ToString
@RequiredArgsConstructor
public class MessageController {
    @Autowired
    private static UserService userService;
    @Autowired
    private static ProjectDetailSerivce projectDetailSerivce;
    @Autowired
    private static MessageService messageService;
    @Autowired
    private HttpSession session;

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

    public ModelAndView modelAndView(String html) {
        ModelAndView mav = new ModelAndView(html);
        return mav;
    }


    @GetMapping("/project/recvMessageList")
    public ModelAndView viewRecvMessage(HttpSession session, Model model) {
        UserDto userDto = getSessionUser();
        ProjectDto projectDto = getSessionProject();
        List<MessageDto> messageDtoList = messageService.selectAllRecv(userDto, projectDto);

        model.addAttribute("List", messageDtoList);
        return modelAndView("recvMessageList");
    }

    @GetMapping("/project/sendMessageList")
    public ModelAndView viewSendMessage(HttpSession session, Model model) {
        UserDto userDto = getSessionUser();
        ProjectDto projectDto = getSessionProject();
        List<MessageDto> messageDtoList = messageService.selectAllSend(userDto, projectDto);

        model.addAttribute("List", messageDtoList);

        return modelAndView("sendMessageList");
    }

    @GetMapping("/project/messageForm")
    public ModelAndView sendMessageForm(Model model) {
        List<UserDto> userDtos = userService.searchUserToProject(getSessionProject().getProjectId());
        model.addAttribute("userList", userDtos);
        return modelAndView("messageForm");
    }

    @RequestMapping("/message/{id}")
    public ModelAndView selectMessage(@PathVariable("id") Long id, Model model) {
        MessageDto messageDto = messageService.selectMessage(id);
        model.addAttribute("message", messageDto);
        return modelAndView("messageDetail");
    }

    @PostMapping("/sendMessage")
    public ModelAndView sendMessage(@RequestParam(value = "title") String title,
                              @RequestParam(value = "content") String content,
                              @RequestParam(value = "recvName") String name,
                              HttpSession session) {
        log.info(name + "입니다.");
        messageService.sendMessage(title, content, getSessionUser(), name, getSessionProject());
        return modelAndView("redirect:/sendMessageList");
    }
}
