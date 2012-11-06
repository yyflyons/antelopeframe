package com.ifunshow.antelopeframe.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ifunshow.antelopeframe.entity.system.SystemLoginUser;
import com.ifunshow.antelopeframe.web.base.BaseController;

/**
 * 欢迎页面
 * @author yyflyons-于亚丰
 */
@Controller
@RequestMapping("/login")
public class LoginController extends BaseController{
	
	@RequestMapping("/plogin")
	public String pageLogin(Model model,String preUrl){
		model.addAttribute("preUrl", preUrl);
		return "login/pageLogin";
	}
	
	@RequestMapping("/alogin")
	public String ajaxLogin(HttpServletRequest request){
		return "login/ajaxLogin";
	}

	@RequestMapping("/signin")
	public String login(HttpServletRequest request,@Valid SystemLoginUser user, BindingResult result,String preUrl){
		System.out.println("======>"+preUrl);
		request.getSession().setAttribute("current_user", new Object());
		return "redirect:"+(StringUtils.isNotBlank(preUrl)?preUrl:"/");
	}
	
	@RequestMapping("/signout")
	public String unlogin(HttpServletRequest request){
		request.getSession().removeAttribute("current_user");
		return "redirect:/";
	}
	
}
