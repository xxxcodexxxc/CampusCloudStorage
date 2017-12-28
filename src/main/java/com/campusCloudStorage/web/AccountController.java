package com.campusCloudStorage.web;

import com.campusCloudStorage.entity.User;
import com.campusCloudStorage.enums.LoginStateEnum;
import com.campusCloudStorage.enums.RegisterStateEnum;
import com.campusCloudStorage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(){
        return "login";
    }

    @RequestMapping(value="/login",method= RequestMethod.POST)
    public String login(User user, HttpServletRequest request, RedirectAttributes attributes, RedirectAttributesModelMap modelMap) {
        LoginStateEnum loginState=userService.validate(user);

        //登录状态不成功，则返回登录界面重新登录
        if(loginState!=LoginStateEnum.SUCCESS){
            return "login";
        }

        //登录成功，进入首页
        int uId=user.getuId();
        User userFromDB=userService.getUserById(uId);
        int rootDir=userFromDB.getRootDir();

        HttpSession session=request.getSession();
        session.setAttribute("uId",uId);
        session.setAttribute("rootDir",userFromDB.getRootDir());//设置根目录session
        session.setAttribute("recyclebin",userFromDB.getRecyclebin());//设置回收站根目录session
        modelMap.addFlashAttribute("uId",uId);
        attributes.addFlashAttribute("uId",uId);
        return "forward:/home/"+rootDir;
    }

    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String register(){
        return "register";
    }

    @RequestMapping(value="/register",method= RequestMethod.POST)
    public String register(User user,Model model) {
        RegisterStateEnum registerState = userService.register(user);
        model.addAttribute("msg",registerState.getStateInfo());
        //注册成功，返回登录界面
        if(registerState==RegisterStateEnum.SUCCESS){
            return "login";
        }
        //注册不成功，重新返回注册界面
        model.addAttribute("user",user);
        return "register";
    }

}
