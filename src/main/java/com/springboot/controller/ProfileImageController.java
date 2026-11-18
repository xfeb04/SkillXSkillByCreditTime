package com.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

@RestController
public class ProfileImageController {

    private final MemberRepository memberRepository;

    public ProfileImageController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/member/profile-image")
    public ResponseEntity<byte[]> profileImage(HttpSession session) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return memberRepository.findById(memberId)
                .filter(member -> member.getImgProfile() != null && member.getImgProfile().length > 0)
                .map(member -> ResponseEntity.ok().contentType(detectImageType(member.getImgProfile())).body(member.getImgProfile()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}/profile-image")
    public ResponseEntity<byte[]> profileImage(@PathVariable Integer memberId) {
        return memberRepository.findById(memberId)
                .filter(member -> member.getImgProfile() != null && member.getImgProfile().length > 0)
                .map(member -> ResponseEntity.ok().contentType(detectImageType(member.getImgProfile())).body(member.getImgProfile()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private MediaType detectImageType(byte[] image) {
        if (image.length >= 8 && image[0] == (byte) 0x89 && image[1] == 0x50) return MediaType.IMAGE_PNG;
        if (image.length >= 3 && image[0] == 'G' && image[1] == 'I' && image[2] == 'F') return MediaType.IMAGE_GIF;
        return MediaType.IMAGE_JPEG;
    }
}
