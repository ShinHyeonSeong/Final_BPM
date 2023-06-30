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

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@Slf4j
@ToString
@RequiredArgsConstructor
public class MessageController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectDetailSerivce projectDetailSerivce;
    @Autowired
    private MessageService messageService;
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

    /* - - - - - Message Contorller - - - - - - */
    @GetMapping("/recvMessageList")
    public String viewRecvMessage(HttpSession session, Model model) {
        UserDto userDto = getSessionUser();
        ProjectDto projectDto = getSessionProject();
        List<MessageDto> messageDtoList = messageService.selectAllRecv(userDto, projectDto);

        model.addAttribute("List", messageDtoList);
        return "recvMessageList";
    }

    @GetMapping("/sendMessageList")
    public String viewSendMessage(HttpSession session, Model model) {
        UserDto userDto = getSessionUser();
        ProjectDto projectDto = getSessionProject();
        List<MessageDto> messageDtoList = messageService.selectAllSend(userDto, projectDto);

        model.addAttribute("List", messageDtoList);

        return "sendMessageList";
    }

    @GetMapping("/messageForm")
    public String sendMessageForm(Model model) {
        List<UserDto> userDtos = userService.searchUserToProject(getSessionProject().getProjectId());
        model.addAttribute("userList", userDtos);
        return "messageForm";
    }

    @RequestMapping("/message/{id}")
    public String selectMessage(@PathVariable("id") Long id, Model model) {
        MessageDto messageDto = messageService.selectMessage(id);
        model.addAttribute("message", messageDto);

        return "messageDetail";
    }

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestParam(value = "title") String title,
                              @RequestParam(value = "content") String content,
                              @RequestParam(value = "recvName") String name,
                              HttpSession session) {
        log.info(name + "입니다.");
        messageService.sendMessage(title, content, getSessionUser(), name, getSessionProject());
        return "redirect:/sendMessageList";
    }
}
