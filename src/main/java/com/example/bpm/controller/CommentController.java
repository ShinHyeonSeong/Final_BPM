package com.example.bpm.controller;

import com.example.bpm.dto.ProjectDto;
import com.example.bpm.dto.UserDto;
import com.example.bpm.dto.WorkCommentDto;
import com.example.bpm.dto.WorkDto;
import com.example.bpm.entity.UserEntity;
import com.example.bpm.entity.WorkEntity;
import com.example.bpm.service.ProjectDetailSerivce;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@Slf4j
@ToString
@RequiredArgsConstructor
public class CommentController {

    @Autowired
    private ProjectDetailSerivce projectDetailSerivce;
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

    /* - - - - 댓글 관련 메서드 - - - -*/
    @PostMapping("/workDetail/addComment")
    public ModelAndView plusComment(@RequestParam("workId") Long workId,
                              @RequestParam("comment") String comment,
                              HttpSession session, Model model, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        /* 댓글을 추가 시키는 메서드 */
        WorkDto workDto = projectDetailSerivce.selectWork(workId);
        UserDto nowUser = getSessionUser();
        WorkCommentDto workCommentDto = new WorkCommentDto();
        workCommentDto.setComment(comment);
        workCommentDto.setWorkIdToComment(WorkEntity.toWorkEntity(workDto));
        workCommentDto.setUserIdToComment(UserEntity.toUserEntity(nowUser));
        /* 댓글을 추가 시키는 메서드 끝 */

        /*추가 시킬 댓글 내용과, 현재 documentID 를 같이 넘겨 리턴 값으로 자동 리스트를 뽑아온다*/
        List<WorkCommentDto> list = projectDetailSerivce.plusComment(workCommentDto, workId);
        model.addAttribute("commentList", list);
        return modelAndView("redirect:" + referer);
    }

    //댓글 수정을 하기 위한 댓글 데이터를 가져오는 메서드 (프론트에서는 댓글을 수정할 수 있는 화면이 필요하다
    @GetMapping("/workDetail/commentUpdate")
    public ModelAndView updateForm(@RequestParam("commentId") Long commentId, Model model) {
        WorkCommentDto updateComment = projectDetailSerivce.findComment(commentId);
        model.addAttribute("updateComment", updateComment);

        return modelAndView("");
    }

    @PostMapping("댓글 수정 완료 시")
    public String updateComment(@RequestParam("workId") Long workId,
                                @RequestParam("comment") String comment, HttpSession session, Model model) {

        WorkDto workDto = projectDetailSerivce.selectWork(workId);
        UserDto nowUser = getSessionUser();
        WorkCommentDto workCommentDto = new WorkCommentDto();
        workCommentDto.setComment(comment);
        workCommentDto.setWorkIdToComment(WorkEntity.toWorkEntity(workDto));
        workCommentDto.setUserIdToComment(UserEntity.toUserEntity(nowUser));

        List<WorkCommentDto> list = projectDetailSerivce.plusComment(workCommentDto, workId);
        model.addAttribute("commentList", list);
        return "";
    }

    @RequestMapping("/workDetail/commentDelete/{cid}")
    public ModelAndView deleteComment(@PathVariable("cid") Long commentId, Model model) {
        WorkCommentDto workCommentDto = projectDetailSerivce.findComment(commentId);
        Long workId = workCommentDto.getWorkIdToComment().getWorkId();
        List<WorkCommentDto> dtoList = projectDetailSerivce.deleteComment(commentId, workId);
        model.addAttribute("CommentList", dtoList);
        return modelAndView("redirect:/project/work/detail/" + workId);
    }
    /* - - - - 댓글 관련 메서드 끝 - - - -*/


}
