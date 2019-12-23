package com.xtm.action;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/user")
@Controller
public class UserController {
    /**
     * 注解权限
     * @return
     */
//    @RequiresPermissions("user:show")
    @RequestMapping("/show")
    public String showUser() {
        return "/user/show";
    }

    /**
     * 注解权限
     * @return
     */
//    @RequiresPermissions("user:show1")
    @RequestMapping("/show1")
    @ResponseBody
    public String showUser1() {
        return "/user/show1";
    }
}
