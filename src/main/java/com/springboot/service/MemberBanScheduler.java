package com.springboot.service;

import com.springboot.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class MemberBanScheduler {
    private final MemberRepository members;
    public MemberBanScheduler(MemberRepository members) { this.members = members; }
    @Scheduled(fixedDelay = 10000, initialDelay = 1000)
    public void releaseExpiredBans() {
        members.releaseExpiredBans(null, LocalDate.now(ZoneId.of("Asia/Bangkok")));
    }
}
