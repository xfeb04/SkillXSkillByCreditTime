package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.springboot.model.*;
import com.springboot.repository.RegisterCourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.server.ResponseStatusException;

class ReviewCourseControllerTest {
    final RegisterCourseRepository repository = mock(RegisterCourseRepository.class);
    final com.springboot.repository.TeacherRepository teachers = mock(com.springboot.repository.TeacherRepository.class);
    final Teacher teacher = new Teacher();
    final ReviewCourseController controller = new ReviewCourseController(repository, mock(com.springboot.repository.CourseDateRepository.class), teachers);
    final RegisterCourse registration = new RegisterCourse();
    final MockHttpSession session = new MockHttpSession();
    ReviewCourseControllerTest() {
        session.setAttribute("memberId", 7);
        var learner = mock(Student.class); when(learner.getMemberId()).thenReturn(7);
        teacher.setMemberId(8);
        when(teachers.findForUpdate(8)).thenReturn(Optional.of(teacher));
        when(repository.averageRatingForTeacher(8)).thenReturn(4.6666666667);
        var category = new Category(); category.setCategoryName("ภาษา");
        var course = new Course(); course.setTeacher(teacher); course.setCategory(category); course.setCourseStatus("สอนเสร็จสิ้น");
        registration.setCourse(course); registration.setMember(learner); registration.setRegisterStatus("อนุมัติแล้ว");
        when(repository.findForCancellation(7, 12)).thenReturn(Optional.of(registration));
    }
    @Test void savesRatingWithOptionalCommentAndDate() {
        controller.submit(12,"5","",session,new ExtendedModelMap(),new RedirectAttributesModelMap());
        assertEquals(5.0, registration.getRatingScore()); assertNull(registration.getComment());
        assertNotNull(registration.getReviewDate()); verify(repository).saveAndFlush(registration);
        assertFalse(ReviewCourseController.canReview(registration));
        assertEquals(new java.math.BigDecimal("4.67"), teacher.getAvgRatingScore());
        var order = inOrder(teachers, repository);
        order.verify(teachers).findForUpdate(8);
        order.verify(repository).saveAndFlush(registration);
        order.verify(repository).averageRatingForTeacher(8);
        order.verify(teachers).save(teacher);
        assertThrows(ResponseStatusException.class, () -> controller.submit(12,"1","",session,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        verify(teachers,times(1)).save(teacher);
    }
    @Test void rejectsMissingOrInvalidRatingAndLongComment() {
        for (String rating : new String[]{"", "0", "6", "NaN", "1.5"}) {
            var model = new ExtendedModelMap();
            assertEquals("reviewCourse", controller.submit(12,rating,"",session,model,new RedirectAttributesModelMap()));
            assertTrue(model.containsAttribute("ratingError"));
        }
        var model = new ExtendedModelMap();
        controller.submit(12,"3","a".repeat(256),session,model,new RedirectAttributesModelMap());
        assertTrue(model.containsAttribute("commentError")); verify(repository,never()).saveAndFlush(any());
    }
    @Test void deniesPendingRegistrationAndOtherMembers() {
        registration.setRegisterStatus("รออนุมัติ");
        assertThrows(ResponseStatusException.class, () -> controller.submit(12,"5","",session,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        session.setAttribute("memberId", 9);
        assertThrows(ResponseStatusException.class, () -> controller.submit(12,"5","",session,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        verify(repository,never()).saveAndFlush(any());
    }
}
