package com.springboot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.springboot.SpringBootApplicationMain;
import com.springboot.repository.AdminRepository;
import com.springboot.repository.CategoryRepository;
import com.springboot.repository.CourseDateRepository;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.MemberRepository;
import com.springboot.repository.RegisterCourseRepository;
import com.springboot.repository.ReportRepository;
import com.springboot.repository.SkillRepository;
import com.springboot.repository.StudentRepository;
import com.springboot.repository.TeacherRepository;

public class Run {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SpringBootApplicationMain.class, args);

		AdminRepository adminRepository = context.getBean(AdminRepository.class);
		CategoryRepository categoryRepository = context.getBean(CategoryRepository.class);
		MemberRepository memberRepository = context.getBean(MemberRepository.class);
		TeacherRepository teacherRepository = context.getBean(TeacherRepository.class);
		StudentRepository studentRepository = context.getBean(StudentRepository.class);
		SkillRepository skillRepository = context.getBean(SkillRepository.class);
		CourseRepository courseRepository = context.getBean(CourseRepository.class);
		CourseDateRepository courseDateRepository = context.getBean(CourseDateRepository.class);
		RegisterCourseRepository registerCourseRepository = context.getBean(RegisterCourseRepository.class);
		CreditTransactionRepository creditTransactionRepository = context
				.getBean(CreditTransactionRepository.class);
		ReportRepository reportRepository = context.getBean(ReportRepository.class);

		

		// 1. Admin
		Admin admin1 = new Admin(null, "admin01", "admin1234");
		adminRepository.save(admin1);
		System.out.println("เพิ่ม Admin: " + admin1.getUsername());

		// 2. Category
		Category catCooking = new Category(null, "การทำอาหาร", "ทักษะเกี่ยวกับการทำอาหารทุกประเภท");
		categoryRepository.save(catCooking);

		Category catMusic = new Category(null, "ดนตรี", "ทักษะเกี่ยวกับดนตรีและเครื่องดนตรี");
		categoryRepository.save(catMusic);

		Category catLanguage = new Category(null, "ภาษา", "ทักษะด้านภาษาต่างประเทศ");
		categoryRepository.save(catLanguage);
		System.out.println("เพิ่ม Category ครบ 3 หมวด");

		// 3. สร้าง Member (ยังไม่ save แยก — ปล่อยให้ cascade จาก Teacher/Student จัดการ insert ให้)
		Member member1 = new Member(null, "teacher01", "pass1234", "สมชาย", "ใจดี", "somchai@example.com",
				"0812345678", "male", null, LocalDate.of(1990, 5, 10), LocalDate.now(), 0.0, 0.0, 0, null, null);

		Member member2 = new Member(null, "teacher02", "pass1234", "วิภา", "รักดนตรี", "wipha@example.com",
				"0823456789", "female", null, LocalDate.of(1992, 8, 20), LocalDate.now(), 0.0, 0.0, 0, null, null);

		Member member3 = new Member(null, "student01", "pass1234", "สมหญิง", "ตั้งใจเรียน", "somying@example.com",
				"0898765432", "female", null, LocalDate.of(2000, 3, 15), LocalDate.now(), 10.0, 0.0, 0, null, null);

		// สมมติ member4 เป็นทั้ง Teacher และ Student พร้อมกัน (เพราะไม่มี login แยกกัน)
		Member member4 = new Member(null, "student02", "pass1234", "ธนากร", "ขยันเรียน", "thanakorn@example.com",
				"0887654321", "male", null, LocalDate.of(1999, 11, 2), LocalDate.now(), 5.0, 0.0, 0, null, null);

		// 4. Teacher (composition — cascade จะ insert Member ให้พร้อมกันในทีเดียว)
		Teacher teacher1 = new Teacher(member1, "สอนทำอาหารไทยมากว่า 10 ปี", 4.8);
		teacherRepository.save(teacher1);

		Teacher teacher2 = new Teacher(member2, "สอนกีตาร์และเปียโน", 4.5);
		teacherRepository.save(teacher2);
		System.out.println("เพิ่ม Teacher: " + teacher1.getMember().getUsername() + ", "
				+ teacher2.getMember().getUsername());

		// 5. Student (composition — cascade จะ insert Member ให้พร้อมกันในทีเดียว)
		Student student1 = new Student(member3);
		studentRepository.save(student1);

		Student student2 = new Student(member4);
		studentRepository.save(student2);
		System.out.println("เพิ่ม Student: " + student1.getMember().getUsername() + ", "
				+ student2.getMember().getUsername());

		// 5. Skill (ผูกกับ Teacher และ Category)
		Skill skill1 = new Skill(null, teacher1, catCooking, "ทำต้มยำกุ้ง", "สอนวิธีทำต้มยำกุ้งแบบต้นตำรับ", null);
		skillRepository.save(skill1);

		Skill skill2 = new Skill(null, teacher2, catMusic, "เล่นกีตาร์เบื้องต้น", "สอนกีตาร์ตั้งแต่พื้นฐาน", null);
		skillRepository.save(skill2);
		System.out.println("เพิ่ม Skill ครบ 2 รายการ");

		// 6. Course (ผูกกับ Teacher และ Category)
		Course course1 = new Course(null, teacher1, catCooking, "คอร์สทำต้มยำกุ้งเบื้องต้น",
				"เรียนรู้การทำต้มยำกุ้งตั้งแต่พื้นฐานจนถึงขั้นสูง", false, null, "ครัวกลาง มหาวิทยาลัยแม่โจ้",
				"หม้อ, เตาแก๊ส, วัตถุดิบ", 10, "cooking", 2.0, "active", LocalDate.now());
		courseRepository.save(course1);

		Course course2 = new Course(null, teacher2, catMusic, "คอร์สกีตาร์เบื้องต้น",
				"เรียนรู้การเล่นกีตาร์ตั้งแต่จับคอร์ดแรก", true, "Zoom", null, "กีตาร์โปร่งหรือไฟฟ้า", 5, "online",
				1.5, "active", LocalDate.now());
		courseRepository.save(course2);
		System.out.println("เพิ่ม Course ครบ 2 รายการ");

		// 7. CourseDate (ผูกกับ Course)
		CourseDate courseDate1 = new CourseDate(null, course1, LocalDate.now().plusDays(7),
				LocalDateTime.now().plusDays(7).withHour(9).withMinute(0),
				LocalDateTime.now().plusDays(7).withHour(12).withMinute(0));
		courseDateRepository.save(courseDate1);

		CourseDate courseDate2 = new CourseDate(null, course2, LocalDate.now().plusDays(3),
				LocalDateTime.now().plusDays(3).withHour(18).withMinute(0),
				LocalDateTime.now().plusDays(3).withHour(20).withMinute(0));
		courseDateRepository.save(courseDate2);
		System.out.println("เพิ่ม CourseDate ครบ 2 รายการ");

		// 8. RegisterCourse (ผูกกับ Student และ Course)
		RegisterCourse register1 = new RegisterCourse(null, student1, course1, "confirmed", LocalDate.now(), null,
				null, null);
		registerCourseRepository.save(register1);

		RegisterCourse register2 = new RegisterCourse(null, student2, course2, "pending", LocalDate.now(), null,
				null, null);
		registerCourseRepository.save(register2);
		System.out.println("เพิ่ม RegisterCourse ครบ 2 รายการ");

		// 9. CreditTransaction (ผูกกับ RegisterCourse)
		CreditTransaction transaction1 = new CreditTransaction(null, register1, 2.0, LocalDate.now(), "payment",
				"ชำระเครดิตสำหรับคอร์สทำต้มยำกุ้งเบื้องต้น", "success");
		creditTransactionRepository.save(transaction1);
		System.out.println("เพิ่ม CreditTransaction ครบ 1 รายการ");

		// 10. Report (ผูกกับ Teacher, Student, RegisterCourse) — ตัวอย่างเผื่อทดสอบ
		Report report1 = new Report(null, teacher1, student1, register1, "behavior", "มาสายเกิน 30 นาที", null,
				LocalDate.now());
		reportRepository.save(report1);
		System.out.println("เพิ่ม Report ครบ 1 รายการ");

		System.out.println("\n✅ เพิ่มข้อมูลทดสอบเรียบร้อยแล้ว!");
	}

}