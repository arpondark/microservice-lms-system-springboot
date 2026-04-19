package site.shazan.course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.shazan.course.kafka.KafkaProducerService;
import site.shazan.course.models.*;
import site.shazan.course.repo.CourseRepository;
import site.shazan.course.repo.EnrollmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollRepo;
    private final MinioService minio;
    private final KafkaProducerService kafkaProducerService;

    @Value("${minio.image-bucket}")
    private String imageBucket;

    @Value("${minio.video-bucket}")
    private String videoBucket;

    @Value("${minio.material-bucket}")
    private String materialBucket;

    public Course create(
            Course course,
            MultipartFile image,
            String videoUrl,
            MultipartFile material,
            Long teacherId
    ) {

        // Upload image only if provided
        if (image != null && !image.isEmpty()) {
            course.setImageUrl(minio.upload(image, imageBucket));
        }

        // Set video URL directly (can be a link)
        if (videoUrl != null && !videoUrl.isEmpty()) {
            course.setVideoUrl(videoUrl);
        }

        // Upload material only if provided
        if (material != null && !material.isEmpty()) {
            course.setCourseMaterialUrl(minio.upload(material, materialBucket));
        }

        course.setTeacherId(teacherId);
        course.setStatus("PUBLISHED");

        Course savedCourse = courseRepo.save(course);

        // Publish course creation event
        kafkaProducerService.publishCourseCreatedEvent(
            savedCourse.getId().toString(),
            savedCourse.getCourseName(),
            teacherId.toString()
        );

        return savedCourse;
    }

    public List<Course> getAll() {
        return courseRepo.findAll();
    }

    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepo.findByTeacherId(teacherId);
    }

    public Enrollment enroll(Long studentId, Long courseId) {

        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setCourseId(courseId);

        Enrollment savedEnrollment = enrollRepo.save(e);

        // Publish enrollment event
        kafkaProducerService.publishEnrollmentEvent(savedEnrollment);

        return savedEnrollment;
    }
}


