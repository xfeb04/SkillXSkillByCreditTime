package com.springboot.model;


import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.springboot.SpringBootApplicationMain;
import com.springboot.repository.AdminRepository;
import com.springboot.repository.CategoryRepository;
import com.springboot.repository.CourseDateRepository;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.RegisterCourseRepository;
import com.springboot.repository.ReportRepository;
import com.springboot.repository.SkillRepository;
import com.springboot.repository.TeacherRepository;

public class Run {
    // Fictional demo biographies, awards, venues and meeting links.
    // Re-running this demo refreshes its schedules relative to the Bangkok date.
    private static final LocalDate DEMO_TODAY = LocalDate.now(java.time.ZoneId.of("Asia/Bangkok"));
    private static final java.util.Map<Integer, List<CourseDate>> SEED_DATES = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> SEED_DATE_INDEX = new java.util.HashMap<>();


	public static void main(String[] args) {
        SEED_DATES.clear();
        SEED_DATE_INDEX.clear();
		ApplicationContext context = SpringApplication.run(SpringBootApplicationMain.class, args);

		AdminRepository adminRepository = context.getBean(AdminRepository.class);
		CategoryRepository categoryRepository = context.getBean(CategoryRepository.class);
		CourseDateRepository courseDateRepository = context.getBean(CourseDateRepository.class);
		CourseRepository courseRepository = context.getBean(CourseRepository.class);
		CreditTransactionRepository creditTransactionRepository = context.getBean(CreditTransactionRepository.class);
		RegisterCourseRepository registerCourseRepository = context.getBean(RegisterCourseRepository.class);
		ReportRepository reportRepository = context.getBean(ReportRepository.class);
		SkillRepository skillRepository = context.getBean(SkillRepository.class);
		TeacherRepository teacherRepository = context.getBean(TeacherRepository.class);
		JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);

		// ddl-auto=update จะไม่ขยาย VARCHAR เดิมเป็น LONGBLOB/LONGTEXT เสมอไป
		// จึงปรับ schema เก่าก่อนสร้างข้อความ report ตัวอย่างที่มีความยาวเกิน 255 ตัวอักษร
		ensureReportReasonSupportsLongText(jdbcTemplate);
        jdbcTemplate.execute("ALTER TABLE report MODIFY COLUMN imgReport LONGBLOB");
		ensureTeacherRatingUsesTwoDecimalPlaces(jdbcTemplate);
		ensureProfileImageSupportsFiveMegabytes(jdbcTemplate);
        // Widen legacy bio storage before writing Thai demo biographies; preserve existing text.
        jdbcTemplate.execute("ALTER TABLE teacher MODIFY COLUMN bio LONGTEXT NULL");
        // Existing schemas may retain short text columns despite ddl-auto=update.
        jdbcTemplate.execute("ALTER TABLE skill MODIFY COLUMN skillDescription LONGTEXT NULL");
        removeLegacySkillNameColumn(jdbcTemplate);
        jdbcTemplate.execute("ALTER TABLE course MODIFY COLUMN courseDescription LONGTEXT NULL");
		ensureSkillImageSupportsFiveMegabytes(jdbcTemplate);
        jdbcTemplate.execute("ALTER TABLE course MODIFY COLUMN onlinePlatform VARCHAR(255) NULL");

        migrateCourseCategoryRelation(jdbcTemplate);
        var seedTransaction = new org.springframework.transaction.support.TransactionTemplate(
                context.getBean(org.springframework.transaction.PlatformTransactionManager.class));
        seedTransaction.executeWithoutResult(status -> {
		// 1. Admin
		Admin admin1 = new Admin(null, "admin01", "admin1234");
		if (!adminRepository.existsByUsername(admin1.getUsername())) {
			adminRepository.save(admin1);
		}
		

        // 2. Each specific category is directly selectable.
        var categories = new java.util.LinkedHashMap<String, Category>();
        String[][] categoryGroups = {
            { "อาหารไทย", "อาหารญี่ปุ่น", "อาหารจีน", "อาหารเกาหลี", "อาหารอิตาเลียน", "อาหารอินเดีย", "อาหารมังสวิรัติ", "อาหารเพื่อสุขภาพ", "ขนมไทย", "ทำเค้ก", "ทำขนมปัง", "ทำคุกกี้", "การชงกาแฟ", "การชงชา", "การผสมเครื่องดื่ม" },
            { "กีตาร์โปร่ง", "กีตาร์ไฟฟ้า", "เบส", "เปียโน", "คีย์บอร์ด", "กลองชุด", "ไวโอลิน", "อูคูเลเล่", "แซกโซโฟน", "ฟลูต", "ขลุ่ยไทย", "ระนาด", "ร้องเพลง", "แต่งเพลง", "ผลิตเพลง" },
            { "ภาษาอังกฤษ", "ภาษาจีนกลาง", "ภาษาญี่ปุ่น", "ภาษาเกาหลี", "ภาษาฝรั่งเศส", "ภาษาเยอรมัน", "ภาษาสเปน", "ภาษาอิตาลี", "ภาษาโปรตุเกส", "ภาษารัสเซีย", "ภาษาอาหรับ", "ภาษาเวียดนาม", "ภาษาพม่า", "ภาษาไทย", "ภาษามือไทย" },
            { "HTML และ CSS", "JavaScript", "Python", "Java", "C และ C++", "C#", "PHP", "พัฒนาแอป Android", "พัฒนาแอป iOS", "ฐานข้อมูลและ SQL", "วิเคราะห์ข้อมูล", "ปัญญาประดิษฐ์", "ความปลอดภัยออนไลน์", "ใช้งานคอมพิวเตอร์", "หุ่นยนต์และไมโครคอนโทรลเลอร์" },
            { "วาดเส้น", "วาดภาพสีน้ำ", "วาดภาพสีอะคริลิก", "วาดภาพสีน้ำมัน", "วาดภาพดิจิทัล", "วาดการ์ตูน", "ออกแบบตัวละคร", "ออกแบบโลโก้", "ออกแบบโปสเตอร์", "ออกแบบบรรจุภัณฑ์", "ออกแบบ UI/UX", "สร้างโมเดล 3 มิติ", "แอนิเมชัน", "ถ่ายภาพ", "ตัดต่อวิดีโอ" },
            { "วางแผนธุรกิจ", "บัญชีเบื้องต้น", "คำนวณต้นทุนและตั้งราคา", "บริหารร้านค้า", "ขายสินค้าออนไลน์", "การขายและเจรจาต่อรอง", "บริการลูกค้า", "สร้างแบรนด์", "เขียนคอนเทนต์", "การตลาดโซเชียลมีเดีย", "โฆษณาออนไลน์", "SEO", "วิจัยตลาด", "บริหารโครงการ", "จัดการสต็อกสินค้า" },
            { "ฟุตบอล", "วอลเลย์บอล", "บาสเกตบอล", "ว่ายน้ำ", "แบดมินตัน", "เทนนิส", "เทเบิลเทนนิส", "วิ่ง", "ปั่นจักรยาน", "มวยไทย", "โยคะ", "ฟิตเนสและเวทเทรนนิง", "ปฐมพยาบาล", "การดูแลผิว", "โภชนาการ" },
            { "ถักโครเชต์", "ถักนิตติ้ง", "เย็บผ้า", "ปักผ้า", "ทำสบู่", "ทำเทียนหอม", "งานไม้", "งานหนัง", "ปั้นดินและเซรามิก", "ทำเครื่องประดับ", "จัดดอกไม้", "พับกระดาษ", "ปลูกต้นไม้", "จัดสวน", "การขับรถ" },
        };
        for (String[] group : categoryGroups) {
            for (String name : group) {
                categories.put(name, createOrUpdateCategory(categoryRepository, skillRepository, courseRepository,
                        name, "ทักษะและการเรียนรู้ด้าน" + name));
            }
        }

		// 3. Each demo account has Student and Teacher roles sharing one Member.
		Teacher savedTeacher = createOrUpdateTeacher(teacherRepository, "teacher01", "ผมสมชาย ดูแลครัวอาหารไทยในร้านครอบครัวมา 8 ปี ชอบสอนคนเริ่มทำอาหารให้เข้าใจรสชาติและเลือกวัตถุดิบเป็น เวลาว่างทำขนมส่งงานชุมชน อยากแบ่งปันสูตรที่ทำซ้ำได้จริงในครัวที่บ้าน", "สมชาย", "ใจดี",
				"ชาย", LocalDate.of(1995, 5, 14), "somchai.jaidee@example.com", "0612345678");

		Teacher teacher2 = createOrUpdateTeacher(teacherRepository, "teacher02", "สุดาค่ะ เล่นกีตาร์และร้องเพลงในคาเฟ่มา 7 ปี สอนเด็กและผู้ใหญ่มากกว่า 80 คน ชอบแบ่งเพลงยากเป็นแบบฝึกสั้น ๆ เชื่อว่าทุกคนเล่นเพลงแรกได้โดยไม่ต้องอ่านโน้ตคล่อง", "สุดา", "ดีงาม",
				"หญิง", LocalDate.of(1992, 10, 21), "suda.deengam@example.com", "0812345679");
		Teacher teacher3 = createOrUpdateTeacher(teacherRepository, "teacher03", "ผมอนันต์ เคยทำงานประสานงานกับลูกค้าต่างชาติ 9 ปี สนุกกับการฝึกภาษาอังกฤษจากสถานการณ์จริง กำลังเรียนทำอาหารและงานฝีมือเพื่อเปิดกิจกรรมต้อนรับนักท่องเที่ยวในชุมชน", "อนันต์", "บุญมี", "ชาย",
				LocalDate.of(1988, 8, 9), "anan.boonmee@example.com", "0912345680");
		Teacher teacher4 = createOrUpdateTeacher(teacherRepository, "teacher04", "พลอยค่ะ ทำงานออกแบบบรรจุภัณฑ์ให้กลุ่มสมุนไพรชุมชนมา 6 ปี สนใจการเลือกวัตถุดิบและอ่านฉลากอย่างรอบคอบ ชอบกิจกรรมลงมือทำและแลกเปลี่ยนความคิดเห็นโดยไม่กดดันผู้เรียน", "พลอย", "สุขใจ", "หญิง",
				LocalDate.of(1997, 12, 18), "ploy.sukjai@example.com", "0612345681");
		Teacher teacher5 = createOrUpdateTeacher(teacherRepository, "teacher05", "ผมกิตติพงศ์ ดูแลร้านของฝากและบัญชีต้นทุนมา 10 ปี เคยช่วยกลุ่มอาชีพวางแผนขายสินค้าท้องถิ่น อยากแลกเปลี่ยนเรื่องต้นทุน การตั้งราคา และเรียนทักษะใหม่ไปทำกิจกรรมในร้าน", "กิตติพงศ์", "แสงทอง", "ชาย",
				LocalDate.of(1990, 3, 5), "kittipong.saengthong@example.com", "0812345682");
		Teacher teacher6 = createOrUpdateTeacher(teacherRepository, "teacher06", "ณัฐยาค่ะ เปิดโฮมคาเฟ่มา 5 ปี ดูแลตั้งแต่ชงกาแฟ ถ่ายภาพเมนู ไปจนถึงการตลาดออนไลน์ ชอบทดลองสูตรที่ใช้อุปกรณ์ไม่แพงและสอนคนเริ่มต้นให้เข้าใจต้นทุนต่อแก้ว", "ณัฐยา", "วัฒนกุล", "หญิง",
				LocalDate.of(1994, 11, 12), "nattaya.wattanakun@example.com", "0912345683");
		Teacher teacher7 = createOrUpdateTeacher(teacherRepository, "teacher07", "ผมวิชัย เป็นผู้ช่วยฝึกขับรถในสนามชุมชนมา 8 ปี และจัดกิจกรรมปั่นจักรยานวันหยุด เน้นการสังเกตความเสี่ยงและเคารพผู้ใช้ถนนทุกคน ผู้เริ่มต้นถามซ้ำได้เสมอ", "วิชัย", "ศรีสุข", "ชาย",
				LocalDate.of(1989, 2, 27), "wichai.srisuk@example.com", "0612345684");
		Teacher teacher8 = createOrUpdateTeacher(teacherRepository, "teacher08", "ศิริพรค่ะ ทำงานโครเชต์และสบู่แฮนด์เมดกับกลุ่มอาชีพมา 6 ปี รับทำของชำร่วยและออกบูธงานชุมชน ชอบสอนทีละขั้นให้ผู้เรียนได้ชิ้นงานของตัวเองกลับไปฝึกต่อ", "ศิริพร", "มั่นคง", "หญิง",
				LocalDate.of(2000, 6, 30), "siriporn.mankong@example.com", "0812345685");
		Teacher teacher9 = createOrUpdateTeacher(teacherRepository, "teacher09", "ผมธนวัฒน์ ทำงานดูแลระบบคอมพิวเตอร์มา 5 ปี เคยจัดอบรมความปลอดภัยออนไลน์ให้ร้านค้าและผู้สูงอายุ ชอบอธิบายเรื่องเทคนิคด้วยตัวอย่างใกล้ตัวและแบบฝึกที่ลองทำได้", "ธนวัฒน์", "เจริญผล", "ชาย",
				LocalDate.of(1998, 9, 16), "thanawat.charoenphon@example.com", "0912345686");
		Teacher teacher10 = createOrUpdateTeacher(teacherRepository, "teacher10", "กัญญารัตน์ค่ะ เป็นอาสาสมัครช่วยงานปฐมพยาบาลกิจกรรมชุมชนมา 7 ปี ผ่านการอบรมช่วยชีวิตขั้นพื้นฐาน สนใจแบ่งปันวิธีประเมินเหตุและเรียกความช่วยเหลืออย่างเป็นขั้นตอน", "กัญญารัตน์", "พูนสุข", "หญิง",
				LocalDate.of(1991, 7, 25), "kanyarat.poonsuk@example.com", "0612345687");

		// 4. Skills
		Skill thaiCooking = createOrUpdateSkill(skillRepository, savedTeacher, categories.get("อาหารไทย"), "ทำงานครัวอาหารไทยมา 8 ปี เชี่ยวชาญการเตรียมน้ำสต๊อกและปรุงรสแบบชิมปรับ เคยได้เหรียญเงินประกวดอาหารพื้นบ้านในงานชุมชน และดูแลอาหารงานเลี้ยงขนาด 50 คน");
		Skill thaiDesserts = createOrUpdateSkill(skillRepository, savedTeacher, categories.get("ขนมไทย"), "ทำขนมไทยขายตามสั่งมา 6 ปี ถนัดขนมชั้น ฝอยทอง และขนมถ้วย เคยชนะการประกวดขนมไทยงานตลาดชุมชน ถ่ายทอดการคุมไฟและแก้เนื้อขนมโดยใช้เครื่องครัวทั่วไป");
		Skill guitar = createOrUpdateSkill(skillRepository, teacher2, categories.get("กีตาร์โปร่ง"), "เล่นกีตาร์โปร่งมา 12 ปี สอนมา 7 ปี เคยได้รองชนะเลิศวงดนตรีอะคูสติกงานเยาวชนประจำจังหวัด ถนัดจัดแบบฝึกจับคอร์ดและจังหวะให้เหมาะกับมือใหม่");
		Skill onlineSafety = createOrUpdateSkill(skillRepository, teacher9, categories.get("ความปลอดภัยออนไลน์"), "ดูแลระบบและบัญชีออนไลน์มา 5 ปี จัดเวิร์กช็อปให้ร้านค้า 12 กลุ่ม ถนัดวิเคราะห์ข้อความหลอกลวงและตั้งค่าความปลอดภัย เคยเป็นวิทยากรกิจกรรมรู้ทันภัยดิจิทัลของชุมชน");
		Skill safeDriving = createOrUpdateSkill(skillRepository, teacher7, categories.get("การขับรถ"), "ช่วยฝึกขับรถมา 8 ปี จัดกิจกรรมจำลองจุดอับสายตาและระยะเบรกให้ผู้ขับขี่ใหม่ ถนัดอธิบายการประเมินความเสี่ยง มีประสบการณ์ดูแลฐานความปลอดภัยงานกีฬาชุมชน");
		Skill firstAid = createOrUpdateSkill(skillRepository, teacher10, categories.get("ปฐมพยาบาล"), "อาสางานปฐมพยาบาลมา 7 ปี ผ่านการอบรมช่วยชีวิตขั้นพื้นฐานและฝึกทบทวนทุกปี เคยดูแลจุดปฐมพยาบาลงานวิ่งชุมชน ถนัดสอนการประเมินเหตุและสื่อสารกับหน่วยฉุกเฉิน");
		Skill herbalSoap = createOrUpdateSkill(skillRepository, teacher8, categories.get("ทำสบู่"), "ทำสบู่แฮนด์เมดแบบหลอมเทมา 6 ปี พัฒนาสูตรกับกลุ่มอาชีพและออกบูธกว่า 20 งาน เคยได้รางวัลบรรจุภัณฑ์สร้างสรรค์ในงานชุมชน ถนัดคำนวณต้นทุนและบันทึกการผลิต");
		Skill crochetSkill = createOrUpdateSkill(skillRepository, teacher8, categories.get("ถักโครเชต์"), "ถักโครเชต์มา 9 ปี รับทำของชำร่วยมา 6 ปี เคยได้เหรียญทองงานฝีมือในกิจกรรมอาชีพชุมชน ถนัดอ่านผังลาย คุมความแน่น และแก้ชิ้นงานที่เพิ่มหรือลดห่วงผิด");
		Skill homeCafe = createOrUpdateSkill(skillRepository, teacher6, categories.get("การชงกาแฟ"), "เปิดโฮมคาเฟ่มา 5 ปี พัฒนาสูตรขายจริงมากกว่า 15 เมนู เคยได้รองชนะเลิศแข่งขันเครื่องดื่มสร้างสรรค์งานตลาดชุมชน ถนัดปรับรสชาติและคำนวณต้นทุนต่อแก้ว");
		Skill herbalSkincare = createOrUpdateSkill(skillRepository, teacher4, categories.get("การดูแลผิว"), "ทำงานร่วมกับกลุ่มสมุนไพรชุมชนมา 6 ปี ออกแบบฉลากให้ผลิตภัณฑ์กว่า 20 แบบ ถนัดอ่านส่วนประกอบ วางแผนทดสอบการแพ้ และอธิบายข้อควรระวังโดยไม่กล่าวอ้างรักษาโรค");


        // Images supplied for the named demo skills.
        thaiCooking.setSkillImg(readSeedImage("skills/thaicooking_pic.jpg"));
        thaiDesserts.setSkillImg(readSeedImage("skills/thaiDesserts_pic.jpg"));
        guitar.setSkillImg(readSeedImage("skills/guitar_pic.jpg"));
        onlineSafety.setSkillImg(readSeedImage("skills/onlineSafety_pic.jpg"));
        safeDriving.setSkillImg(readSeedImage("skills/safeDriving.jpg"));
        firstAid.setSkillImg(readSeedImage("skills/firstAid_pic.jpg"));
        herbalSoap.setSkillImg(readSeedImage("skills/herbalSoap_pic.png"));
        crochetSkill.setSkillImg(readSeedImage("skills/crochetSkill.png"));
        homeCafe.setSkillImg(readSeedImage("skills/homeCafe_pic.png"));
        herbalSkincare.setSkillImg(readSeedImage("skills/herbalSkincare_pic.jpg"));
        skillRepository.saveAll(List.of(thaiCooking, thaiDesserts, guitar, onlineSafety, safeDriving, firstAid, herbalSoap, crochetSkill, homeCafe, herbalSkincare));

        createOrUpdateSkill(skillRepository, teacher3, categories.get("ภาษาอังกฤษ"), "ประสานงานลูกค้าต่างชาติ 9 ปี เคยดูแลกลุ่มนักท่องเที่ยวมากกว่า 30 กลุ่ม ถนัดฝึกบทสนทนาต้อนรับ แนะนำสินค้า และแก้ปัญหาเฉพาะหน้า โดยช่วยปรับประโยคให้สุภาพและเป็นธรรมชาติ");
        createOrUpdateSkill(skillRepository, teacher5, categories.get("คำนวณต้นทุนและตั้งราคา"), "ดูแลร้านของฝากมา 10 ปี ช่วยกลุ่มอาชีพทำบัญชีต้นทุน 8 กลุ่ม เคยได้รางวัลแผนธุรกิจชุมชนสร้างสรรค์ ถนัดแยกต้นทุนคงที่ ต้นทุนต่อชิ้น และวางราคาขายที่เหลือกำไรจริง");

		// 5. Demo courses: course credits are independent of lesson duration.
		Course savedCourse = createOrUpdateCourse(courseRepository, "ทำอาหารไทยสำหรับผู้เริ่มต้น",
				"เวิร์กช็อปอาหารไทยสำหรับคนเริ่มเข้าครัว เรียนเลือกวัตถุดิบ เตรียมน้ำสต๊อก ปรุงต้มยำและผัดไทยจากประสบการณ์ร้านอาหาร 8 ปีของสมชาย ฝึกชิมปรับรสและจัดเก็บอาหาร จบแล้วทำอาหารสองเมนูด้วยตัวเองได้", false, null,
				"ครัวศาลากลางหมู่บ้านสุขใจ หมู่ที่ 4 ซอยร่วมใจ 2 ถนนชุมชน ต.หนองหอย อ.เมืองเชียงใหม่ จ.เชียงใหม่ ชั้น 1 ประตูข้างลานจอดรถ (สถานที่สมมติ)", "ผ้ากันเปื้อนและสมุดบันทึก", 12, "ปกติ", 6.0, "เปิดรับสมัคร",
				DEMO_TODAY.plusDays(-2), savedTeacher, categories.get("อาหารไทย"));
		Course thaiDessertCourse = createOrUpdateCourse(courseRepository, "ทำขนมไทยยอดนิยม",
				"เรียนขนมไทยผ่านครัวที่บ้านกับสมชาย ผู้รับทำขนมงานชุมชนมา 6 ปี ฝึกชั่งวัตถุดิบ คุมไฟ ทำฝอยทองและขนมชั้น พร้อมดูเนื้อสัมผัสและแก้ปัญหาขนมไม่เซตตัว ผู้สอนสาธิตใกล้ ๆ และให้เปิดกล้องถามระหว่างฝึก", true, "Google Meet — https://meet.example.com/thai-dessert (ลิงก์ห้องเรียนตัวอย่าง)", null,
				"วัตถุดิบสำหรับทำขนม", 15, "ปกติ", 4.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2),
				savedTeacher, categories.get("ขนมไทย"));
		Course guitarCourse = createOrUpdateCourse(courseRepository, "เล่นกีตาร์เบื้องต้น",
				"คอร์สกีตาร์โปร่งสำหรับผู้ไม่เคยเล่น สุดานำประสบการณ์สอน 7 ปีมาแบ่งเป็นการจับคอร์ด เปลี่ยนคอร์ดและตีจังหวะ ฝึกทีละท่อนกับเพลงง่าย พร้อมปรับท่าจับให้ไม่เกร็ง จบคอร์สเล่นประกอบเพลงแรกได้", false, null, "ห้องดนตรีบ้านเสียงใส เลขที่ 24 หมู่ที่ 3 ตรอกเสียงเพลง ซอย 1 ถนนบ้านใหม่ ต.สุเทพ อ.เมืองเชียงใหม่ จ.เชียงใหม่ ชั้น 2 (สถานที่สมมติ)",
				"กีตาร์ส่วนตัว", 10, "ปกติ", 6.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2), teacher2, categories.get("กีตาร์โปร่ง"));
		Course onlineSafetyCourse = createOrUpdateCourse(courseRepository, "รู้ทันภัยมิจฉาชีพออนไลน์",
				"เรียนรู้ความปลอดภัยดิจิทัลกับธนวัฒน์ ผู้ดูแลระบบมา 5 ปี ฝึกแยกข้อความหลอกลวง ตรวจชื่อเว็บไซต์ ตั้งรหัสผ่านและยืนยันตัวตนสองขั้นตอน ใช้สถานการณ์จำลอง ไม่ต้องเปิดเผยรหัสผ่านหรือข้อมูลบัญชีจริง", true, "Zoom — https://meeting.example.com/online-safety (ลิงก์ห้องเรียนตัวอย่าง)", null,
				"โทรศัพท์หรือคอมพิวเตอร์", 50, "เรียนฟรี", 4.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2), teacher9,
				categories.get("ความปลอดภัยออนไลน์"));
		Course safeDrivingCourse = createOrUpdateCourse(courseRepository, "ขับรถอย่างปลอดภัยบนท้องถนน",
				"กิจกรรมความปลอดภัยสำหรับผู้ขับขี่ใหม่ วิชัยนำประสบการณ์ฝึกขับรถ 8 ปีมาสอนอ่านจุดเสี่ยง เว้นระยะและตรวจรถก่อนเดินทาง ฝึกสังเกตจุดอับในสนามปิด พร้อมแลกเปลี่ยนกรณีใกล้ตัว ไม่ใช่หลักสูตรสอบใบขับขี่", false, null, "ลานฝึกขับขี่ศูนย์ชุมชนปลอดภัย หมู่ที่ 6 ซอยร่วมพัฒนา ถนนสันกลาง ต.สันกลาง อ.สันกำแพง จ.เชียงใหม่ จุดนัดพบหน้าอาคารอเนกประสงค์ (สถานที่สมมติ)",
				"ใบอนุญาตขับรถหรือบัตรประชาชน", 20, "เรียนฟรี", 6.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2),
				teacher7, categories.get("การขับรถ"));
		Course firstAidCourse = createOrUpdateCourse(courseRepository, "ปฐมพยาบาลเบื้องต้นในชีวิตประจำวัน",
				"กัญญารัตน์แบ่งปันประสบการณ์อาสาปฐมพยาบาล 7 ปี เรียนประเมินความปลอดภัยของที่เกิดเหตุ แจ้งตำแหน่งให้หน่วยฉุกเฉิน และดูแลแผลเล็กน้อยผ่านสถานการณ์จำลอง เป็นความรู้เบื้องต้น ไม่ทดแทนการฝึกช่วยชีวิตภาคปฏิบัติ", true, "Microsoft Teams — https://meeting.example.com/first-aid (ลิงก์ห้องเรียนตัวอย่าง)", null, "สมุดบันทึก", 40,
				"เรียนฟรี", 4.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2), teacher10, categories.get("ปฐมพยาบาล"));
		Course herbalSoapCourse = createOrUpdateCourse(courseRepository, "ทำสบู่สมุนไพรเพื่อจำหน่าย",
				"ลงมือทำสบู่แบบหลอมเทกับศิริพรจากประสบการณ์กลุ่มอาชีพ 6 ปี เรียนเลือกวัตถุดิบ ชั่งผสม ขึ้นรูปและออกแบบฉลาก ฝึกบันทึกต้นทุนต่อชิ้น ผู้เรียนรับเครดิตสนับสนุนหลังเรียนจบและร่วมสะท้อนผลการทดลอง", false, null, "ห้องงานฝีมือศาลากลางหมู่บ้านร่มเย็น หมู่ที่ 5 ซอยหัตถกรรม 3 ถนนต้นเปา ต.ต้นเปา อ.สันกำแพง จ.เชียงใหม่ ชั้น 1 ติดห้องสมุด (สถานที่สมมติ)",
				"ผ้ากันเปื้อน", 12, "ผู้สอนจ่ายให้ผู้เรียน", 6.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2), teacher8,
				categories.get("ทำสบู่"));
		Course homeCafeCourse = createOrUpdateCourse(courseRepository, "ชงกาแฟและแนะนำสินค้าโฮมคาเฟ่",
				"ณัฐยาเจ้าของโฮมคาเฟ่ 5 ปีสอนชงกาแฟด้วยอุปกรณ์ที่มี ฝึกสัดส่วนกาแฟ นมและน้ำแข็ง ทดลองปรับรสพร้อมคำนวณต้นทุนต่อแก้ว แลกเปลี่ยนแนวคิดถ่ายภาพเมนู ผู้เรียนรับเครดิตสนับสนุนหลังจบกิจกรรม", true, "Google Meet — https://meet.example.com/home-cafe (ลิงก์ห้องเรียนตัวอย่าง)", null,
				"แก้วและอุปกรณ์ชงกาแฟ", 30, "ผู้สอนจ่ายให้ผู้เรียน", 4.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2),
				teacher6, categories.get("การชงกาแฟ"));
		Course herbalSkincareCourse = createOrUpdateCourse(courseRepository, "ดูแลผิวด้วยสมุนไพรไทย",
				"เวิร์กช็อปอ่านฉลากและเลือกผลิตภัณฑ์สมุนไพรกับพลอย ผู้ทำงานกับกลุ่มอาชีพมา 6 ปี ฝึกสังเกตส่วนผสม ข้อควรระวังและวางแผนทดสอบการแพ้ ไม่กล่าวอ้างรักษาโรค ผู้เรียนรับเครดิตเมื่อจบและให้ความคิดเห็น", false, null, "ห้องกิจกรรมบ้านสมุนไพร เลขที่ 18 หมู่ที่ 2 ตรอกสวนหอม ซอย 4 ถนนชุมชน ต.หนองควาย อ.หางดง จ.เชียงใหม่ อาคารสีขาว (สถานที่สมมติ)",
				"ผ้าเช็ดหน้า", 15, "ผู้สอนจ่ายให้ผู้เรียน", 6.0, "เปิดรับสมัคร", DEMO_TODAY.plusDays(-2), teacher4,
				categories.get("การดูแลผิว"));
		Course crochetCourse = createOrUpdateCourse(courseRepository, "ถักโครเชต์สำหรับผู้เริ่มต้น",
				"ศิริพรนำประสบการณ์ถักโครเชต์ 9 ปีมาสอนจับเข็ม อ่านสัญลักษณ์และคุมความแน่น ฝึกโซ่และลายพื้นฐานจนได้ที่รองแก้วหนึ่งชิ้น พร้อมวิธีนับห่วงและแก้ขอบเบี้ยว เหมาะกับผู้เริ่มต้นที่อยากทำของใช้หรือของขวัญ", false, null, "ห้องงานฝีมือศาลากลางหมู่บ้านร่มเย็น หมู่ที่ 5 ซอยหัตถกรรม 3 ถนนต้นเปา ต.ต้นเปา อ.สันกำแพง จ.เชียงใหม่ ชั้น 1 ติดห้องสมุด (สถานที่สมมติ)",
				"ไหมพรมและเข็มโครเชต์", 10, "ปกติ", 6.0, "สอนเสร็จสิ้น", DEMO_TODAY.plusDays(-25), teacher8,
				categories.get("ถักโครเชต์"));

        removeUnusedLegacyCategories(categoryRepository, skillRepository, courseRepository);

		// 6. Refresh existing demo lesson rows instead of appending duplicate schedules.
		createCourseDate(courseDateRepository, savedCourse, DEMO_TODAY.plusDays(5), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, savedCourse, DEMO_TODAY.plusDays(12), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, thaiDessertCourse, DEMO_TODAY.plusDays(7), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, thaiDessertCourse, DEMO_TODAY.plusDays(14), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, guitarCourse, DEMO_TODAY.plusDays(6), LocalTime.of(13, 0), LocalTime.of(15, 0));
		createCourseDate(courseDateRepository, guitarCourse, DEMO_TODAY.plusDays(13), LocalTime.of(13, 0), LocalTime.of(15, 0));
		createCourseDate(courseDateRepository, guitarCourse, DEMO_TODAY.plusDays(20), LocalTime.of(13, 0), LocalTime.of(15, 0));
		createCourseDate(courseDateRepository, onlineSafetyCourse, DEMO_TODAY.plusDays(8), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, onlineSafetyCourse, DEMO_TODAY.plusDays(15), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, safeDrivingCourse, DEMO_TODAY.plusDays(10), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, safeDrivingCourse, DEMO_TODAY.plusDays(17), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, firstAidCourse, DEMO_TODAY.plusDays(11), LocalTime.of(10, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, firstAidCourse, DEMO_TODAY.plusDays(18), LocalTime.of(10, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, herbalSoapCourse, DEMO_TODAY.plusDays(9), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, herbalSoapCourse, DEMO_TODAY.plusDays(16), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, homeCafeCourse, DEMO_TODAY.plusDays(12), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, homeCafeCourse, DEMO_TODAY.plusDays(19), LocalTime.of(18, 0), LocalTime.of(20, 0));
		createCourseDate(courseDateRepository, herbalSkincareCourse, DEMO_TODAY.plusDays(11), LocalTime.of(13, 0), LocalTime.of(16, 0));
		createCourseDate(courseDateRepository, herbalSkincareCourse, DEMO_TODAY.plusDays(18), LocalTime.of(13, 0), LocalTime.of(16, 0));
		createCourseDate(courseDateRepository, crochetCourse, DEMO_TODAY.plusDays(-14), LocalTime.of(9, 0), LocalTime.of(12, 0));
		createCourseDate(courseDateRepository, crochetCourse, DEMO_TODAY.plusDays(-7), LocalTime.of(9, 0), LocalTime.of(12, 0));

        // Reserve sponsorship budgets before approving any demo learners.
        for (Course sponsored : List.of(herbalSoapCourse, homeCafeCourse, herbalSkincareCourse)) {
            seedCourseReserve(creditTransactionRepository, registerCourseRepository, teacherRepository, sponsored);
        }
        createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, savedCourse, teacher2);
		approveTeacherPaidRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher3,
				herbalSoapCourse, teacher8, DEMO_TODAY.plusDays(-2));
		approveTeacherPaidRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher5,
				homeCafeCourse, teacher6, DEMO_TODAY.plusDays(-2));

		// Normal pending requests include the frozen balance and matching transaction.
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, savedCourse, teacher3, teacher4, teacher5, teacher6);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, thaiDessertCourse, teacher2, teacher3, teacher4, teacher5);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, guitarCourse, teacher3, teacher4, teacher5, teacher6);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, onlineSafetyCourse, savedTeacher, teacher2, teacher3, teacher4);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, safeDrivingCourse, savedTeacher, teacher2, teacher3, teacher4);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, firstAidCourse, savedTeacher, teacher2, teacher3, teacher4);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, herbalSoapCourse, savedTeacher, teacher2, teacher4, teacher5);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, homeCafeCourse, savedTeacher, teacher2, teacher3, teacher4);
		createPendingRegistrations(registerCourseRepository, creditTransactionRepository, teacherRepository, herbalSkincareCourse, savedTeacher, teacher2, teacher3, teacher5);

		// คอร์สปกติที่สอนเสร็จ: ผู้เรียนชำระเครดิตแล้ว และผู้สอนได้รับเครดิตเมื่อสอนจบ
		completeNormalRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, savedTeacher,
				crochetCourse, teacher8, DEMO_TODAY.plusDays(-7));
		completeNormalRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher2,
				crochetCourse, teacher8, DEMO_TODAY.plusDays(-7));
		completeNormalRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher3,
				crochetCourse, teacher8, DEMO_TODAY.plusDays(-7));
		completeNormalRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher4,
				crochetCourse, teacher8, DEMO_TODAY.plusDays(-7));
		completeNormalRegistration(registerCourseRepository, creditTransactionRepository, teacherRepository, teacher5,
				crochetCourse, teacher8, DEMO_TODAY.plusDays(-7));
		addReview(registerCourseRepository, savedTeacher, crochetCourse, 5.0,
				"สอนเข้าใจง่าย ได้ฝึกถักจริงทุกขั้นตอน");
		addReview(registerCourseRepository, teacher3, crochetCourse, 4.0,
				"เนื้อหาดี อธิบายชัดเจน อยากให้มีเวลาเพิ่มอีกเล็กน้อย");
		addReview(registerCourseRepository, teacher5, crochetCourse, 5.0,
				"ได้ชิ้นงานกลับบ้านและสามารถนำไปฝึกต่อได้");
		for (Teacher seededTeacher : List.of(savedTeacher, teacher2, teacher3, teacher4, teacher5,
                teacher6, teacher7, teacher8, teacher9, teacher10)) {
            Double average = registerCourseRepository.averageRatingForTeacher(seededTeacher.getMemberId());
            seededTeacher.setAvgRatingScore(BigDecimal.valueOf(average == null ? 0 : average)
                    .setScale(2, RoundingMode.HALF_UP));
            teacherRepository.save(seededTeacher);
        }

		// 8. Report
		deleteOldSampleReports(reportRepository);
		createReportForCompletedRegistration(reportRepository, registerCourseRepository, teacher2, teacher8, crochetCourse,
				"ผู้สอน", "ใช้คำพูดดูหมิ่น และข่มขู่ผู้เรียนต่อหน้าผู้อื่นระหว่างการสอน",
				DEMO_TODAY.plusDays(-7));
		createReportForCompletedRegistration(reportRepository, registerCourseRepository, teacher4, teacher8, crochetCourse,
				"ผู้สอน", "ผู้สอนบันทึกและเผยแพร่ภาพผู้เรียนโดยไม่ได้รับอนุญาต",
				DEMO_TODAY.plusDays(-7));
        });
        System.out.println("ข้อมูลถูกสร้างเรียบร้อยแล้ว");
		

	}

	private static Course createOrUpdateCourse(CourseRepository courseRepository, String courseName,
			String courseDescription, Boolean isOnline, String onlinePlatform, String location, String requiredEquip,
			Integer maxMember, String courseType, Double creditPerCourse, String courseStatus, LocalDate createdAt,
			Teacher teacher, Category category) {
		Course course = courseRepository.findByCourseNameAndTeacher_MemberId(courseName, teacher.getMemberId())
				.orElseGet(Course::new);
		course.setCourseName(courseName);
		course.setCourseDescription(courseDescription);
		course.setIsOnline(isOnline);
		course.setOnlinePlatform(onlinePlatform);
		course.setLocation(location);
		course.setRequiredEquip(requiredEquip);
		course.setMaxMember(maxMember);
		course.setCourseType(courseType);
		course.setCreditPerCourse("เรียนฟรี".equals(courseType) ? 2.0 : creditPerCourse);
		course.setCourseStatus(courseStatus);
		course.setCreatedAt(createdAt);
		course.setTeacher(teacher);
		course.setCategory(category);
		return courseRepository.save(course);
	}

	private static Category createOrUpdateCategory(CategoryRepository categoryRepository, SkillRepository skillRepository,
            CourseRepository courseRepository,
			String categoryName, String categoryDescription) {
		List<Category> matchingCategories = categoryRepository.findAllByCategoryNameOrderByCategoryIdAsc(categoryName);
		Category primaryCategory = matchingCategories.isEmpty() ? new Category() : matchingCategories.get(0);
		primaryCategory.setCategoryName(categoryName);
		primaryCategory.setCateDescription(categoryDescription);
		Category savedPrimaryCategory = categoryRepository.save(primaryCategory);
		Integer primaryCategoryId = savedPrimaryCategory.getCategoryId();
		for (Category duplicateCategory : matchingCategories) {
			if (duplicateCategory.getCategoryId().equals(primaryCategoryId)) {
				continue;
			}
			skillRepository.findAll().stream()
					.filter(skill -> skill.getCategory().getCategoryId().equals(duplicateCategory.getCategoryId()))
					.forEach(skill -> {
						skill.setCategory(savedPrimaryCategory);
						skillRepository.save(skill);
					});
            courseRepository.findAll().stream()
                    .filter(course -> course.getCategory().getCategoryId().equals(duplicateCategory.getCategoryId()))
                    .forEach(course -> {
                        course.setCategory(savedPrimaryCategory);
                        courseRepository.save(course);
                    });
            skillRepository.flush();
            courseRepository.flush();
            categoryRepository.delete(duplicateCategory);
		}
		return savedPrimaryCategory;
	}

    private static void removeUnusedLegacyCategories(CategoryRepository categories, SkillRepository skills,
            CourseRepository courses) {
        // Keep broad categories still referenced by non-demo data rather than guessing their replacement.
        var legacyNames = java.util.Set.of("การทำอาหาร", "ดนตรี", "ภาษา", "เทคโนโลยีและการเขียนโปรแกรม",
                "ออกแบบและศิลปะ", "ธุรกิจและการตลาด", "สุขภาพและกีฬา", "งานฝีมือและงานอดิเรก");
        skills.flush();
        courses.flush();
        var usedIds = new java.util.HashSet<Integer>();
        skills.findAll().forEach(skill -> usedIds.add(skill.getCategory().getCategoryId()));
        courses.findAll().forEach(course -> usedIds.add(course.getCategory().getCategoryId()));
        categories.findAll().stream()
                .filter(category -> legacyNames.contains(category.getCategoryName()))
                .filter(category -> !usedIds.contains(category.getCategoryId()))
                .forEach(categories::delete);
        categories.flush();
    }

	private static void ensureReportReasonSupportsLongText(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE report MODIFY COLUMN reason LONGTEXT NOT NULL");
	}

	private static void ensureTeacherRatingUsesTwoDecimalPlaces(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE teacher MODIFY COLUMN avgRatingScore DECIMAL(3,2)");
	}

	private static void ensureProfileImageSupportsFiveMegabytes(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE members MODIFY COLUMN imgProfile LONGBLOB");
	}

	private static void ensureSkillImageSupportsFiveMegabytes(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("ALTER TABLE skill MODIFY COLUMN skillImg LONGBLOB");
	}

	/**
	 * ย้ายความสัมพันธ์เดิม course.skill_id -> skill.category_id ไปเป็น
	 * course.category_id แล้วจึงลบ skill_id ออกจากตาราง course.
	 */
	private static void migrateCourseCategoryRelation(JdbcTemplate jdbcTemplate) {
		boolean hasCategoryColumn = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
				"SELECT EXISTS(SELECT 1 FROM information_schema.columns "
						+ "WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'category_id')",
				Boolean.class));
		if (!hasCategoryColumn) {
			jdbcTemplate.execute("ALTER TABLE course ADD COLUMN category_id INT NULL");
		}

		boolean hasSkillColumn = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
				"SELECT EXISTS(SELECT 1 FROM information_schema.columns "
						+ "WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'skill_id')",
				Boolean.class));
		if (hasSkillColumn) {
			jdbcTemplate.execute("UPDATE course c JOIN skill s ON c.skill_id = s.skill_id "
					+ "SET c.category_id = s.category_id WHERE c.category_id IS NULL OR c.category_id = 0");
			Integer unmigratedCourses = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM course WHERE category_id IS NULL OR category_id = 0", Integer.class);
			if (unmigratedCourses != null && unmigratedCourses > 0) {
				throw new IllegalStateException("ไม่สามารถย้าย category_id ของคอร์สเดิมได้ครบทุกคอร์ส");
			}

			List<String> skillForeignKeys = jdbcTemplate.queryForList(
					"SELECT constraint_name FROM information_schema.key_column_usage "
							+ "WHERE table_schema = DATABASE() AND table_name = 'course' "
							+ "AND column_name = 'skill_id' AND referenced_table_name IS NOT NULL",
					String.class);
			for (String foreignKey : skillForeignKeys) {
				jdbcTemplate.execute("ALTER TABLE course DROP FOREIGN KEY `" + foreignKey + "`");
			}
			jdbcTemplate.execute("ALTER TABLE course DROP COLUMN skill_id");
		}

		Integer missingCategory = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM course WHERE category_id IS NULL OR category_id = 0", Integer.class);
		if (missingCategory != null && missingCategory > 0) {
			throw new IllegalStateException("พบคอร์สที่ยังไม่มี category_id");
		}
		jdbcTemplate.execute("ALTER TABLE course MODIFY COLUMN category_id INT NOT NULL");

		Integer categoryForeignKeyCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.key_column_usage "
						+ "WHERE table_schema = DATABASE() AND table_name = 'course' "
						+ "AND column_name = 'category_id' AND referenced_table_name = 'category'",
				Integer.class);
		if (categoryForeignKeyCount != null && categoryForeignKeyCount == 0) {
			jdbcTemplate.execute("ALTER TABLE course ADD CONSTRAINT fk_course_category "
					+ "FOREIGN KEY (category_id) REFERENCES category(category_id)");
		}
	}

	private static void createCourseDate(CourseDateRepository courseDateRepository, Course course, LocalDate teachDate,
			LocalTime startTime, LocalTime endTime) {
        var existing = SEED_DATES.computeIfAbsent(course.getCourseId(),
                id -> courseDateRepository.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(id));
        int index = SEED_DATE_INDEX.getOrDefault(course.getCourseId(), 0);
        var lesson = index < existing.size() ? existing.get(index) : new CourseDate();
        lesson.setCourse(course);
        lesson.setTeachDate(teachDate); lesson.setStartTime(startTime); lesson.setEndTime(endTime);
        courseDateRepository.save(lesson);
        SEED_DATE_INDEX.put(course.getCourseId(), index + 1);
	}

    private static void fundDemoBalance(Teacher member, double required) {
        double balance = member.getCreditBalance() == null ? 0 : member.getCreditBalance();
        if (balance < required) {
            System.out.println("SEED credit grant " + member.getUsername() + ": +" + (required - balance));
            member.setCreditBalance(required);
        }
    }

    private static void createPendingRegistrations(RegisterCourseRepository registrations,
            CreditTransactionRepository transactions, TeacherRepository teachers, Course course, Teacher... learners) {
        for (Teacher learner : learners) {
            var registration = registrations.findByMember_MemberIdAndCourse_CourseId(learner.getMemberId(), course.getCourseId())
                    .orElseGet(() -> registrations.save(new RegisterCourse("รออนุมัติ", DEMO_TODAY.minusDays(2),
                            null, null, null, learner, course, null)));
            if (!"รออนุมัติ".equals(registration.getRegisterStatus()) || !"ปกติ".equals(course.getCourseType())) continue;
            if (transactions.findByRegisterCourse_RegisterCourseId(registration.getRegisterCourseId()).isPresent()) continue;
            // Missing legacy seed transactions never froze credits. Fund and reserve them together now.
            double amount = course.getCreditPerCourse();
            fundDemoBalance(learner, amount);
            learner.setCreditBalance(learner.getCreditBalance() - amount);
            learner.setCreditFrozen((learner.getCreditFrozen() == null ? 0 : learner.getCreditFrozen()) + amount);
            teachers.save(learner);
            var transaction = transactions.save(new CreditTransaction(amount, registration.getRegisteredAt(),
                    "ชำระค่าเรียน", "พักเครดิตค่าเรียนคอร์ส " + course.getCourseName() + " (seed)",
                    "รอผู้สอนอนุมัติ", registration));
            registration.setCreditTransaction(transaction);
            registrations.save(registration);
        }
    }

    private static void seedCourseReserve(CreditTransactionRepository transactions,
            RegisterCourseRepository registrations, TeacherRepository teachers, Course course) {
        if (transactions.findCourseReserveForUpdate(course.getCourseId()).isPresent()) return;
        var teacher = course.getTeacher();
        double allocated = 0;
        for (var r : registrations.findByCourse_CourseIdAndRegisterStatus(course.getCourseId(), "อนุมัติแล้ว")) {
            var tx = transactions.findByRegisterCourse_RegisterCourseId(r.getRegisterCourseId()).orElseThrow(
                    () -> new IllegalStateException("Approved seed registration has no credit transaction"));
            allocated += tx.getAmount();
            if ("รอการสอนเสร็จ".equals(tx.getCreditTranStatus())) {
                // Legacy seed reserved the approved learner's amount rather than the whole course.
                double frozen = teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen();
                if (frozen < tx.getAmount()) throw new IllegalStateException("Legacy seed frozen credits do not match");
                teacher.setCreditFrozen(frozen - tx.getAmount());
                tx.setCreditTranStatus("รอสอนเสร็จ");
                tx.setCreditTranType("ผู้สอนจ่ายให้ผู้เรียน");
                transactions.save(tx);
            }
        }
        double remaining = course.getMaxMember() * course.getCreditPerCourse() - allocated;
        if (remaining < 0) throw new IllegalStateException("Seed course budget is smaller than allocations");
        fundDemoBalance(teacher, remaining);
        teacher.setCreditBalance(teacher.getCreditBalance() - remaining);
        teacher.setCreditFrozen((teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen()) + remaining);
        teachers.save(teacher);
        var hold = new CreditTransaction();
        hold.setReservedCourse(course); hold.setAmount(remaining);
        hold.setCreditTranDate(DEMO_TODAY);
        hold.setCreditTranType("พักเครดิตผู้สอน"); hold.setCreditTranStatus("พักเครดิตคอร์ส");
        hold.setTranDescription("พักเครดิตคอร์ส " + course.getCourseName() + " (seed)");
        transactions.save(hold);
    }

    private static void approveTeacherPaidRegistration(RegisterCourseRepository registrations,
            CreditTransactionRepository transactions, TeacherRepository teachers, Teacher learner,
            Course course, Teacher teacher, LocalDate registeredAt) {
        if (registrations.findByMember_MemberIdAndCourse_CourseId(learner.getMemberId(), course.getCourseId()).isPresent()) return;
        double amount = course.getCreditPerCourse();
        var hold = transactions.findCourseReserveForUpdate(course.getCourseId()).orElseThrow();
        if (hold.getAmount() < amount || teacher.getCreditFrozen() < amount) throw new IllegalStateException("Seed reserve insufficient");
        hold.setAmount(hold.getAmount() - amount);
        teacher.setCreditFrozen(teacher.getCreditFrozen() - amount);
        teachers.save(teacher); transactions.save(hold);
        var registration = registrations.save(new RegisterCourse("อนุมัติแล้ว", registeredAt, null,
                null, null, learner, course, null));
        var tx = transactions.save(new CreditTransaction(amount, registeredAt, "ผู้สอนจ่ายให้ผู้เรียน",
                "คอร์ส " + course.getCourseName() + " รอส่งมอบเครดิต (seed)", "รอสอนเสร็จ", registration));
        registration.setCreditTransaction(tx);
        registrations.save(registration);
    }

	private static void completeNormalRegistration(RegisterCourseRepository registerCourseRepository,
			CreditTransactionRepository creditTransactionRepository, TeacherRepository teacherRepository, Teacher learner,
			Course course, Teacher teacher, LocalDate completedAt) {
		if (registerCourseRepository.findByMember_MemberIdAndCourse_CourseId(learner.getMemberId(), course.getCourseId())
				.isPresent()) {
			return;
		}

		Double amount = course.getCreditPerCourse();
        fundDemoBalance(learner, amount);

		learner.setCreditBalance(learner.getCreditBalance() - amount);
		teacher.setCreditBalance(teacher.getCreditBalance() + amount);
		teacherRepository.save(learner);
		teacherRepository.save(teacher);

		RegisterCourse registration = registerCourseRepository.save(new RegisterCourse("เรียนเสร็จสิ้น", completedAt, null,
				null, null, learner, course, null));
		String transactionDescription = "ชำระค่าเรียนคอร์ส" + course.getCourseName() + " ของผู้เรียน " + learner.getUsername();
		CreditTransaction transaction = creditTransactionRepository.findByTranDescription(transactionDescription)
				.orElseGet(() -> creditTransactionRepository.save(new CreditTransaction(amount, completedAt, "ชำระค่าเรียน",
						transactionDescription, "สำเร็จ", registration)));
		registration.setCreditTransaction(transaction);
		registerCourseRepository.save(registration);
	}

	private static void addReview(RegisterCourseRepository registerCourseRepository, Teacher learner, Course course,
			Double ratingScore, String comment) {
		registerCourseRepository.findByMember_MemberIdAndCourse_CourseId(learner.getMemberId(), course.getCourseId())
				.ifPresent(registration -> {
					if ("เรียนเสร็จสิ้น".equals(registration.getRegisterStatus()) && registration.getRatingScore() == null) {
						registration.setRatingScore(ratingScore);
                        registration.setReviewDate(DEMO_TODAY.minusDays(6));
						registration.setComment(comment);
						registerCourseRepository.save(registration);
					}
				});
	}

	private static void deleteOldSampleReports(ReportRepository reportRepository) {
		reportRepository.findAll().stream()
				.filter(report -> "ข้อมูลรายงานตัวอย่าง".equals(report.getReason()) || "Example report data".equals(report.getReason()))
				.forEach(reportRepository::delete);
	}

	private static void createReportForCompletedRegistration(ReportRepository reportRepository,
			RegisterCourseRepository registerCourseRepository, Teacher reporter, Teacher reportedMember, Course course,
			String reportType, String reason, LocalDate reportDate) {
		registerCourseRepository.findByMember_MemberIdAndCourse_CourseId(reporter.getMemberId(), course.getCourseId())
				.filter(registration -> "เรียนเสร็จสิ้น".equals(registration.getRegisterStatus()))
				// Report seed ต้องรันซ้ำได้ และฐานข้อมูลเดิมอาจกำหนด register_course_id เป็น unique
				.filter(registration -> !reportRepository
						.existsByRegisterCourse_RegisterCourseId(registration.getRegisterCourseId()))
				.ifPresent(registration -> reportRepository.save(
						new Report(reportType, reason, null, reportDate, registration.getCourse().getTeacher(), registration.getMember(), registration)));
	}

    private static byte[] readSeedImage(String relativePath) {
        String path = "static/picture/" + relativePath;
        var resource = new org.springframework.core.io.ClassPathResource(path);
        try (var input = resource.getInputStream()) {
            byte[] bytes = input.readNBytes(5 * 1024 * 1024 + 1);
            if (bytes.length == 0 || bytes.length > 5 * 1024 * 1024) {
                throw new IllegalStateException("Seed image must be nonempty and at most 5 MB: " + path);
            }
            return bytes;
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read seed image: " + path, ex);
        }
    }

	private static Teacher createOrUpdateTeacher(TeacherRepository teacherRepository, String username, String bio,
			String firstname, String surname, String gender, LocalDate birthDay, String email, String phoneNumber) {
		Teacher teacher = teacherRepository.findByUsername(username).orElseGet(Teacher::new);
		teacher.setUsername(username);
		teacher.setPassword("teacher1234");
		teacher.setBio(bio);
        if ("ชาย".equals(gender)) teacher.setImgProfile(readSeedImage("profiles/men.jpg"));
        else if ("หญิง".equals(gender)) teacher.setImgProfile(readSeedImage("profiles/women.jpg"));
		if (teacher.getAvgRatingScore() == null) teacher.setAvgRatingScore(BigDecimal.ZERO.setScale(2));
		teacher.setFirstname(firstname);
		teacher.setSurname(surname);
		teacher.setGender(gender);
		teacher.setBirthDay(birthDay);
		teacher.setEmail(email);
		teacher.setPhoneNumber(phoneNumber);
		if (teacher.getCreatedAt() == null) {
			teacher.setCreatedAt(DEMO_TODAY.minusMonths(8));
		}
		if (teacher.getCreditBalance() == null) {
			teacher.setCreditBalance((double) java.util.concurrent.ThreadLocalRandom.current().nextInt(7, 16));
		}
		if (teacher.getCreditFrozen() == null) {
            teacher.setCreditFrozen(0.0);
        }
		if (teacher.getBanStatus() == null) teacher.setBanStatus(0);
        teacher.getStudent();
		return teacherRepository.save(teacher);
	}

	private static Skill createOrUpdateSkill(SkillRepository skillRepository, Teacher teacher, Category category,
			String skillDescription) {
		Skill skill = skillRepository.findFirstByTeacher_MemberIdAndCategory_CategoryIdOrderBySkillIdAsc(
				teacher.getMemberId(), category.getCategoryId()).orElseGet(Skill::new);
		skill.setSkillDescription(skillDescription);
		skill.setTeacher(teacher);
		skill.setCategory(category);
		return skillRepository.save(skill);
	
		
	}

    private static void removeLegacySkillNameColumn(JdbcTemplate jdbcTemplate) {
        Boolean exists = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = 'skill' AND column_name = 'skillName')", Boolean.class);
        if (Boolean.TRUE.equals(exists)) jdbcTemplate.execute("ALTER TABLE skill DROP COLUMN skillName");
    }

}

		
