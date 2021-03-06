package com.xtm.config.shiro;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import java.util.HashSet;
import java.util.Set;

public class CustomRealm extends AuthorizingRealm {
    /**
     * 权限认证，即登录过后，每个身份不一定，对应的所能看的页面也不一样。
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//        String username = (String) SecurityUtils.getSubject().getPrincipal();
        String username = (String) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        //这里权限应该从数据库获取
        Set<String> stringSet = new HashSet<>();
        stringSet.add("user:show");
        stringSet.add("user:admin");
        //设置用户角色,这里不区分，正式开发需要区分
        simpleAuthorizationInfo.setRoles(stringSet);
        //设置访问路径权限
        simpleAuthorizationInfo.setStringPermissions(stringSet);
        return simpleAuthorizationInfo;
    }

    /**
     * 身份认证。即登录通过账号和密码验证登陆人的身份信息。
     * 这里可以注入userService,为了方便演示，我就写死了帐号了密码
     * private UserService userService;
     * <p>
     * 获取即将需要认证的信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("-------身份认证方法--------");
        String userName = (String) authenticationToken.getPrincipal();
        String userPwd = new String((char[]) authenticationToken.getCredentials());
        userPwd = MD5Pwd(userName,userPwd);
        //根据用户名从数据库获取密码
        String password = "2415b95d3203ac901e287b76fcef640b";// cj 123
        if (userName == null) {
            throw new AccountException("用户名不正确");
        } else if (!userPwd.equals(password )) {
            throw new AccountException("密码不正确");
        }
        // 把当前用户存到 Session 中
//        SecurityUtils.getSubject().getSession().setAttribute("user", user);
        //交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配
        return new SimpleAuthenticationInfo(userName, password, ByteSource.Util.bytes(userName + "salt"), getName());
//        return new SimpleAuthenticationInfo(userName, password,getName());
    }

    public static String MD5Pwd(String username, String pwd) {
        // 加密算法MD5
        // salt盐 username + salt
        // 迭代次数
        String md5Pwd = new SimpleHash("MD5", pwd,
                ByteSource.Util.bytes(username + "salt"), 2).toHex();
        return md5Pwd;
    }

}
