package site.shazan.course.dto;

import lombok.Data;

@Data
public class CourseRequest {
    private String courseName;
    private String courseCode;
    private String courseDescription;
    private String imageUrl;
    private String teacherName;
    private String videoUrl;
    private String courseMaterialUrl;
}
