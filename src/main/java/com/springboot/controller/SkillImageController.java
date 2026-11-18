package com.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.model.Skill;
import com.springboot.repository.SkillRepository;

import jakarta.servlet.http.HttpSession;

@RestController
public class SkillImageController {

    private final SkillRepository skillRepository;

    public SkillImageController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @GetMapping("/skill/{skillId}/image")
    public ResponseEntity<byte[]> skillImage(@PathVariable Integer skillId, HttpSession session) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return skillRepository.findById(skillId)
                .filter(skill -> skill.getSkillImg() != null && skill.getSkillImg().length > 0)
                .map(skill -> ResponseEntity.ok().contentType(detectImageType(skill.getSkillImg())).body(skill.getSkillImg()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private MediaType detectImageType(byte[] image) {
        if (image.length >= 8 && image[0] == (byte) 0x89 && image[1] == 0x50) return MediaType.IMAGE_PNG;
        if (image.length >= 3 && image[0] == 'G' && image[1] == 'I' && image[2] == 'F') return MediaType.IMAGE_GIF;
        return MediaType.IMAGE_JPEG;
    }
}
