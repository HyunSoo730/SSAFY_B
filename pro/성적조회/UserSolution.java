package B;

import java.util.HashMap;
import java.util.TreeSet;

class UserSolution {

    private static class Student implements Comparable<Student> {

        int id, grade, gender, score;

        Student(int id, int grade, int gender, int score) {
            this.id = id;
            this.grade = grade;
            this.gender = gender;
            this.score = score;
        }

        @Override
        public int compareTo(Student o) {
            if (this.score == o.score) {
                return o.id - this.id; // ! 점수 같으면 ID 큰 사람 (내림차순)
            }
            return o.score - this.score; // ! 점수 큰 사람 (내림차순)
        }
    }

    private static class StudentManager {

        TreeSet<Student>[][] students; // ! 학년별, 성별에 따라서 따로 관리하기 위해.

        StudentManager() {
            students = new TreeSet[4][2]; // ! [grade] : 1,2,3 , [gender] : 0 -> 남자 , 1-> 여자
            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 1; j++) {
                    students[i][j] = new TreeSet<>();
                }
            }
        }

        void add(Student student) {
            students[student.grade][student.gender].add(student);
        }

        // ! TreeSet에서 해당 학생 제거.
        void remove(Student student) {
            students[student.grade][student.gender].remove(student);
        }

        // ! add 함수 호출 시 발동. grade, gender인 애들 중 점수가 가장 높은 학생의 ID 반환
        // ! 점수가 가장 높은 학생이 여러명 -> 그 중에서 ID 가장 큰 값 반환.
        // ! 이미 TreeSet에 정렬조건 반영되어 있음. -> 맨 앞 반환.
        // ! TreeSet에서 집합이 비어있으면 에러 반환. 따라서 예외처리 필요.
        Student getHighestScore(int grade, int gender) {
            TreeSet<Student> set = students[grade][gender];
            if (set.isEmpty()) {
                return null;
            } else {
                return set.first();
            }
        }

        // ! 2. 삭제 후 과정
        // !  2-1. 삭제 후, mId 학생의 학년과 성별이 동일한 학생 중 점수가 가장 낮은 학생 ID 반환
        // !  점수가 가장 낮은 학생이 여러 명 -> ID 가장 작은 값 반환
        // !  2-2. 삭제 후, 학년과 성별이 동일한 학생이 없다면, 0 반환
        public Student getLowestScore(int grade, int gender) {
            TreeSet<Student> set = students[grade][gender]; // ! 학년과 성별이 동일한 학생들 모음
            if (set.isEmpty()) {
                return null;
            } else {
                return set.last(); // ! 점수가 가장 낮은 학생. (만약 같은 애들이 많아도 ID가 가장 작은 애)
            }

        }

        public Student getLowestUpScore(int grade, int gender, int mScore) {
            TreeSet<Student> set = students[grade][gender];
            if (set.isEmpty()) {
                return null;
            } else {
                // ! mScore 이상의 점수를 가진 가장 낮은 점수의 학생 찾기. (ID최하)
                Student temp = new Student(-1, grade, gender, mScore); // ! 점수가 같을 수도 있으니 ID가...
//                Student result = set.ceiling(temp);
                // ! 나는 지금 정렬 조건이 !!! 스코어 내림차순, ID 내림차순이잖아. 그래서 ceiling이 아니라 floor를 통해서 바로 앞에 것. 즉 점수크고 ID 최소인 놈을 찾았어야했음 !!!!
                Student result = set.floor(temp);
                return result;
            }
        }
    }

    static StudentManager studentManager;
    static HashMap<Integer, Student> studentMap;

    public void init() {
        studentManager = new StudentManager();
        studentMap = new HashMap<>();
    }

    // ! 반환하는 것은 같은 grade, gender인 애들 중 점수가 가장 높은 학생의 ID 반환
    // ! 점수가 가장 높은 학생이 여러명 -> 그 중에서 ID 가장 큰 값 반환
    public int add(int mId, int mGrade, char mGender[], int mScore) {
//        System.out.println("Adding: ID=" + mId + ", Grade=" + mGrade + ", Gender=" + new String(mGender) + ", Score=" + mScore);
        int gender = mGender[0] == 'm' ? 0 : 1;
        Student student = new Student(mId, mGrade, gender, mScore);
        studentManager.add(student);
        studentMap.put(mId, student); // ! 기억하기 위해 해시맵에 담아두기 .

        // ! ID 반환하기
        Student topStudent = studentManager.getHighestScore(mGrade, gender);
        if (topStudent == null) {
//            System.out.println("add result : " + mId);
            return mId; // ! 아무도 없으면 본인.
        } else {
//            System.out.println("add result : " + topStudent.id);
            return topStudent.id;
        }
    }

    // ! 학생 ID가 mID인 학생 기록 삭제 -> Map에 기록되어 있는 애 바로 삭제.
    // ! 1. 시스템에 mId 없으면 0 반환
    // ! 2. 삭제 후 과정
    // !  2-1. 삭제 후, mId 학생의 학년과 성별이 동일한 학생 중 점수가 가장 낮은 학생 ID 반환
    // !  점수가 가장 낮은 학생이 여러 명 -> ID 가장 작은 값 반환
    // !  2-2. 삭제 후, 학년과 성별이 동일한 학생이 없다면, 0 반환
    public int remove(int mId) {
        // ! 1. HashMap에서 학생 정보 가져오기
        Student student = studentMap.get(mId);
        if (student == null) {
            return 0; // * 학생 없으면 0 반환
        }
        // ! TreeSet에서 학생 제거
        studentManager.remove(student);
        // ! HashMap에서 학생 제거
        studentMap.remove(mId);

        // ! 2. 삭제 후 과정
        // ! 2-1 점수가 가장 낮은 학생. 그 중 ID가장 작은. -> last
        Student lowerStudent = studentManager.getLowestScore(student.grade, student.gender);
        if (lowerStudent == null) {
            return 0;
        } else {
            return lowerStudent.id;
        }
    }

    // ! 주어진 학년들과 성별들 중, 특정 점수(mScore) 이상인 학생들 중 가장 낮은 점수를 가진 학생ID 반환
    // ! 만약 같은 점수 학생 여러명 -> 그 중 낮은 ID 반환
    // ! 조건 만족하는 학생 없으면 0 반환
    // * mGradeCnt 개의 학년이 mGrade 배열에 ex.{1,3}, mGenderCnt 개의 셩별이 mGender 배열에. ex. {"male", "female"}
    public int query(int mGradeCnt, int mGrade[], int mGenderCnt, char mGender[][], int mScore) {
//        Debug.printTreeSet(studentManager.students);
        Student result = null;
        int lowestScore = Integer.MAX_VALUE;

        // ! 주어진 모든 학년과 성별 조합에 대해 반복
        for (int i = 0; i < mGradeCnt; i++) {
            for (int j = 0; j < mGenderCnt; j++) {
                int grade = mGrade[i];
                int gender = mGender[j][0] == 'm' ? 0 : 1; // ! 성별 확인.

                // ! 현재 학년, 성별 조합에서 mScore 이상인 점수를 가진 학생 중 가장 낮은 점수의 학생 찾기.
                Student candidate = studentManager.getLowestUpScore(grade, gender, mScore);

                // ! 1. candidate가 널이 아니고, (조건 만족하는 학생 존재)
                // ! 2. result가 null이거나, candidate의 점수가 더 낮거나 점수가 같고, ID가 더 낮은 경우 -> result 갱신
                if (candidate == null) {
                    continue;
                }
                if (result == null || candidate.score < result.score ||
                        (candidate.score == result.score && candidate.id < result.id)) {
                    result = candidate;
                }
            }
        }

        if (result == null) {
//            System.out.println("query result : " + 0);
            return 0;
        } else {
//            System.out.println("query result : " + result.id);
            return result.id;
        }

    }

    static class Debug {

        static void printTreeSet(TreeSet<Student>[][] treeSetArr) {
            for (int i = 1; i < 4; i++) {
                for (int j = 0; j < 2; j++) {
//                    System.out.print(i + "학년" + " " + j+" : ");
                    TreeSet<Student> treeSet = treeSetArr[i][j];

                    for (Student student : treeSet) {
//                        System.out.print(student.id + "-" + student.score+" ");
                    }
//                    System.out.println();

                }
            }
        }
    }
}
