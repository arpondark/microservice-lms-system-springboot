package site.shazan.course.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String courseCode;

    @Column(length = 2000)
    private String courseDescription;

    private String imageUrl;
    private String videoUrl;
    private String courseMaterialUrl;

    private Long teacherId;
    private String teacherName;

    private String status;
}