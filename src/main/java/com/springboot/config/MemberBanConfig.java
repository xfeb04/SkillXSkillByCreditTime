package com.springboot.config;

import com.springboot.repository.MemberRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class MemberBanConfig implements WebMvcConfigurer {
    private final MemberRepository members;
    public MemberBanConfig(MemberRepository members) { this.members = members; }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                var session = request.getSession(false);
                if (session == null) return true;
                if (session.getAttribute("bannedMemberId") != null) {
                    response.sendRedirect(request.getContextPath() + "/banned");
                    return false;
                }
                if (!(session.getAttribute("memberId") instanceof Integer id)) return true;
                members.releaseExpiredBans(id, java.time.LocalDate.now(java.time.ZoneId.of("Asia/Bangkok")));
                var member = members.findById(id).orElse(null);
                if (member != null && Integer.valueOf(1).equals(member.getBanStatus())) {
                    session.setAttribute("bannedMemberId", id);
                    session.removeAttribute("memberId"); session.removeAttribute("memberName");
                    session.removeAttribute("memberInitial"); session.removeAttribute("memberHasProfileImage");
                    response.sendRedirect(request.getContextPath() + "/banned");
                    return false;
                }
                return true;
            }
        }).excludePathPatterns("/admin/**", "/css/**", "/js/**", "/images/**", "/error", "/banned", "/loginMember", "/logoutMember");
    }
}
