package com.ppdai.das.console.config;


import com.ppdai.das.console.api.UserConfiguration;
import com.ppdai.das.console.common.user.UserContext;
import com.ppdai.das.console.config.annotation.CurrentUser;
import com.ppdai.das.console.dao.LoginUserDao;
import com.ppdai.das.console.dto.entry.das.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 增加方法注入，将含有 @CurrentUser 注解的方法参数注入当前登录用户
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String UNKNOWN = "unknown";
    public static final Long UNKNOWNID = 0L;

    @Autowired
    private UserConfiguration userConfiguration;

    @Autowired
    private LoginUserDao loginUserDao;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(LoginUser.class) && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        LoginUser user = (LoginUser) webRequest.getAttribute(UserContext.CURRENTUSER, RequestAttributes.SCOPE_SESSION);

        // 统一登录
        if (userConfiguration.isUseSSO() && user == null) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
            HttpServletRequest request = servletWebRequest.getRequest();
            HttpServletResponse response = servletWebRequest.getResponse();
            String userName = userConfiguration.getUserIdentity(request, response).getUserName();
            if(null != userName){
                LoginUser loginUser = loginUserDao.getUserByUserName(userName);
                if (request.getSession().getAttribute(UserContext.CURRENTUSER) == null && null != loginUser) {
                    request.getSession().setAttribute(UserContext.CURRENTUSER, loginUser);
                }
                return loginUser;
            }
        }
        if (user == null) {
            return LoginUser.builder()
                    .id(UNKNOWNID)
                    .userNo(UNKNOWN)
                    .userName(UNKNOWN)
                    .userEmail(UNKNOWN)
                    .build();
        }
        return user;
        //throw new MissingServletRequestPartException("currentUser");
    }
}
