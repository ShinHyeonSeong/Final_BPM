package com.example.bpm.controller;

import com.example.bpm.dto.ProjectDto;
import com.example.bpm.dto.UserDto;
import com.example.bpm.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@ToString
@RequiredArgsConstructor
public class CalenderController {
    @Autowired
    private static CalendarService calendarService;
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

    public ModelAndView modelAndView(String html) {
        ModelAndView mav = new ModelAndView(html);
        return mav;
    }

    @GetMapping("/project/calender") //기본 페이지 표시
    public ModelAndView viewCalendar() {
        return modelAndView("calendar");
    }

    @RequestMapping(value = "/calendar/event", method = {RequestMethod.GET}) //ajax 데이터 전송 URL
    public @ResponseBody List<Map<String, Object>> getEvent() {

        ProjectDto projectDto = getSessionProject();

        return calendarService.getEventList(projectDto.getProjectId());
    }

}
