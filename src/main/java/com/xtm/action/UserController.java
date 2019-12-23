package com.xtm.action;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@Controller
public class UserController {
    /**
     * 注解权限
     * @return
     */
    @RequiresPermissions("user:show")
    @RequestMapping("/show")
    public String showUser() {
        return "/user/show";
    }
}
