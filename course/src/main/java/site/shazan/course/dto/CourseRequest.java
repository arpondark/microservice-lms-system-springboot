package site.shazan.course.dto;

import lombok.Data;

@Data
public class CourseRequest {
    private String courseName;
    private String courseCode;
    private String courseDescription;
}