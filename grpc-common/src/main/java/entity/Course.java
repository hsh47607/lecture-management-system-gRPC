package entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

@Data @AllArgsConstructor
public class Course implements Serializable {
	private static final long serialVersionUID = 1L;

	private String courseId;
    private String profName;
    private String courseName;
    private ArrayList<String> prerequisite;

    public Course(String inputString) {
		StringTokenizer stringTokenizer = new StringTokenizer(inputString);
		this.courseId = stringTokenizer.nextToken();
		this.profName = stringTokenizer.nextToken();
		this.courseName = stringTokenizer.nextToken();
		this.prerequisite = new ArrayList<>();
		while(stringTokenizer.hasMoreTokens()) {
			this.prerequisite.add(stringTokenizer.nextToken());
		}
	}

    public boolean match(String courseId) {
        return this.courseId.equals(courseId);
    }

    @Override
    public String toString() {
        String stringReturn = this.courseId + " " + this.profName + " " + this.courseName;
        for (int i = 0; i < this.prerequisite.size(); i++) {
            stringReturn = stringReturn + " " + this.prerequisite.get(i).toString();
        }
        return stringReturn;
    }

    public static Course toEntity(ClientServer.Course dto) {
        return new Course(
                dto.getId(),
                dto.getProfName(),
                dto.getCourseName(),
                (ArrayList<String>) dto.getPrerequisiteList()
        );
    }

	
}
