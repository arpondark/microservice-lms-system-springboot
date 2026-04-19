package site.shazan.course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    @Value("${minio.image-bucket}")
    private String imageBucket;

    @Value("${minio.video-bucket}")
    private String videoBucket;

    @Value("${minio.material-bucket}")
    private String materialBucket;

    public Course create(
            Course course,
            MultipartFile image,
            MultipartFile video,
            MultipartFile material,
            Long teacherId
    ) {

        course.setImageUrl(minio.upload(image, imageBucket));
        course.setVideoUrl(minio.upload(video, videoBucket));
        course.setCourseMaterialUrl(minio.upload(material, materialBucket));

        course.setTeacherId(teacherId);
        course.setStatus("PUBLISHED");

        return courseRepo.save(course);
    }

    public List<Course> getAll() {
        return courseRepo.findAll();
    }

    public Enrollment enroll(Long studentId, Long courseId) {

        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setCourseId(courseId);

        return enrollRepo.save(e);
    }
}
