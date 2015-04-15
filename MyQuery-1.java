/*****************************
*****************************/
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.Date;
import java.lang.String;

public class MyQuery {

    private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
    
    public MyQuery(Connection c)throws SQLException
    {
        conn = c;
        // Statements allow to issue SQL queries to the database
        statement = conn.createStatement();
    }
    
    public void findGPAInfo() throws SQLException
    {
        String query  = "create temporary table gradeCredit " +
                            "select id, case" + 
					 "when grade = \'A\' then 4 " + 
                            "when grade = \'A-\' then 3.67 " +
                            "when grade = \'B+\' then 3.33 " +
                            "when grade = \'B\' then 3 " +
                            "when grade = \'B-\' then 2.67 " +
                            "when grade = \'C+\' then 2.33 " +
                            "when grade = \'C\' then 2 " +
                            "when grade = \'C-\' then 1.67 " +
                            "when grade = \'D+\' then 1.33 " +
                            "when grade = \'D\' then 1 " +
                            "when grade = \'D-\' then 0.67 " +
                            "else 0 end numGrade, " + 
                            "case when grade is null then 0 else credits end credits " +
                            "from takes join course using (course_id);";

        statement.executeUpdate(query);
	query  = "select id, name, sum(numGrade*credits)/sum(credits) GPA " +
                        "from gradeCredit natural join student " +
                        "group by id;";
        
        resultSet = statement.executeQuery(query);
    }
    
    public void printGPAInfo() throws IOException, SQLException
    {
	System.out.println("******** Query 1 ********");
        while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g. resultSet.getSTring(2);
			String id = resultSet.getString("id");
			String name = resultSet.getString("name");
			String GPA = resultSet.getString("GPA");
			System.out.println(id + " " + name + " " + GPA);
		}        
    }

    public void findMorningCourses() throws SQLException
    {
	String query = "create temporary table allCourse " +
			"select course_id, sec_id, title, semester, year, name " +
			"from course natural join section natural join time_slot natural join teaches natural join instructor  " +
			"where start_hr <=12;";

        statement.executeUpdate(query);
	query = "select course_id, sec_id, title, semester, year, name, count(distinct id) as enrollment "+
		"from allCourse natural join takes "+
		"group by course_id, sec_id, semester, year;";

        resultSet = statement.executeQuery(query);
    }

    public void printMorningCourses() throws IOException, SQLException
    {
	System.out.println("******** Query 2 ********");
        while (resultSet.next()) {
                        String course_id = resultSet.getString("course_id");
                        String sec_id = resultSet.getString("sec_id");
                        String title = resultSet.getString("title");
                        String semester = resultSet.getString("semester");
                        String year = resultSet.getString("year");
                        String name = resultSet.getString("name");
                        String enrollment = resultSet.getString("enrollment");
                        System.out.println(course_id+" "+sec_id+" "+title+" "+semester+" "+year+" "+name+" "+enrollment);
                }
    }

    public void findBusyInstructor() throws SQLException
    {
        String query = " create temporary table courseCount select count(*) cnt from teaches group by id;";

        statement.executeUpdate(query);
        query = " SELECT name FROM teaches NATURAL JOIN instructor GROUP BY id having count(*) = (select max(cnt) from courseCount);";

        resultSet = statement.executeQuery(query);
    }

    public void printBusyInstructor() throws IOException, SQLException
    {
	System.out.println("******** Query 3 ********");
        while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        System.out.println(name);
                }
    }

    public void findPrereq() throws SQLException
    {
        String query = " SELECT c1.title course, case when prereq_id is null then \'\' when prereq_id is not null "+
			"then (select c2.title from course c2 where c2.course_id = prereq_id) end prereq "+
			"from course c1 LEFT OUTER JOIN prereq  "+
			"USING ( course_id );";

        resultSet = statement.executeQuery(query);
    }

    public void printPrereq() throws IOException, SQLException
    {
	System.out.println("******** Query 4 ********");
        while (resultSet.next()) {
                        String course = resultSet.getString("course");
                        String prereq = resultSet.getString("prereq");
                        System.out.println(course+" "+prereq);
                }
    }


    public void updateTable() throws SQLException
    {
        String query = "drop table if exists studentCopy;";
        statement.executeUpdate(query);
    	query = "create table studentCopy select * from student;";
        statement.executeUpdate(query);
        query = "update studentCopy S set tot_cred = coalesce(( select sum(credits) "+
		 "from takes natural join course "+                            
		"where S.ID= takes.ID and takes.grade <> \'F\' and takes.grade is not null), 0);";

        statement.executeUpdate(query);
		
		query = "select * from studentCopy;";
		resultSet = statement.executeQuery(query);
    }

    public void printUpdatedTable() throws IOException, SQLException
    {
	System.out.println("******** Query 5 ********");

        while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String name = resultSet.getString("name");
                        String deptName = resultSet.getString("dept_name");
						String totCredit = resultSet.getString("tot_cred");
                        System.out.println(id+" "+name+" "+deptName+" "+totCredit);
                } 

    }

    public void findFirstLastSemester() throws SQLException
    {
        String query = "create temporary table firstLast select id, min(year) firstYear, max(year) lastYear from takes group by id;";
        statement.executeUpdate(query);

        query = "create temporary table firstYearSemesters "+
		"select id, case when semester = \'Fall\' then 3 when semester = \'Summer\' then 2 when semester = \'Spring\' then 1 end semesterNumber "+
		"from takes natural join firstLast where year = firstYear;";
        statement.executeUpdate(query);

        query = "create temporary table lastYearSemesters "+
		"select id, case when semester = 'Fall' then 3 when semester = 'Summer' then 2 when semester = 'Spring' then 1 end semesterNumber "+
		"from takes natural join firstLast where year = lastYear;";
        statement.executeUpdate(query);

	query = "select id, name, concat(case when min(f.semesterNumber) = 1 then 'Spring' when min(f.semesterNumber) = 2 "+
		"then 'Summer' when min(f.semesterNumber) = 3 then 'Fall' end, ' ', cast(firstYear as char(4))) First_Semester, "+
		"concat(case when max(l.semesterNumber) = 1 then 'Spring' when max(l.semesterNumber) = 2 "+
		"then 'Summer' when max(l.semesterNumber) = 3 then 'Fall' end, ' ', cast(lastYear as char(4))) Last_Semester "+
		"from firstYearSemesters f join firstLast using (id) join lastYearSemesters l using (id) join student using (id) "+
		"group by id";

        resultSet = statement.executeQuery(query);
    }

    public void printFirstLastSemester() throws IOException, SQLException
    {
       System.out.println("******** Query 6 ********");
       while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String First_Semester = resultSet.getString("First_Semester");
                        String name = resultSet.getString("name");
                        String Last_Semester = resultSet.getString("Last_Semester");
                        System.out.println(id+" "+name+" "+First_Semester+" "+Last_Semester);
        }
    }
	
	public void findHeadCounts() throws SQLException
	{
		System.out.println("******** Query 7 ********");	
		InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader bufRead = new BufferedReader(istream) ;
		
		try{
            System.out.println("Please enter the department name for the query: ");
            String deptName = bufRead.readLine();  
			String query = "call getNumbers(?, ?, ?)";
			CallableStatement cstmt = conn.prepareCall(query);
			cstmt.setString(1, deptName);
			cstmt.registerOutParameter(2, java.sql.Types.INTEGER);
			cstmt.registerOutParameter(3, java.sql.Types.INTEGER);
			cstmt.executeQuery();
			int insNumber = cstmt.getInt(2);
			int stuNumber = cstmt.getInt(3);
			System.out.println(deptName+" Department has "+insNumber+" instructors.");
			System.out.println(deptName+" Department has "+stuNumber+" students.");
		}			
		catch (IOException err) {
               System.out.println("Error reading line");
        }
	}
}
