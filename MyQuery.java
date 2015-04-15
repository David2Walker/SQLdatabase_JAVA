package javadb;

/*****************************
*****************************/
import java.io.*;
import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.Date;
import java.util.Scanner;
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
    	resultSet = statement.executeQuery("SELECT ID, student.name, CASE "
          + "WHEN grade = \"A\" "
          + "THEN sum(4*credits)/sum(credits) "
          + "WHEN grade = \"A-\" "
  		  + "THEN sum(3.67*credits)/sum(credits) "
  		  + "WHEN grade = \"B+\" "
		  + "THEN sum(3.33*credits)/sum(credits) "
		  + "WHEN grade = \"B\" "
		  + "THEN sum(3*credits)/sum(credits) "
		  + "WHEN grade = \"B-\" "
		  + "THEN sum(2.67*credits)/sum(credits) "
		  + "WHEN grade = \"C+\" "
		  + "THEN sum(2.33*credits)/sum(credits) "
		  + "WHEN grade = \"C\" "
		  + "THEN sum(2*credits)/sum(credits) "
		  + "WHEN grade = \"C-\" "
		  + "THEN sum(1.67*credits)/sum(credits) "
		  + "WHEN grade = \"D+\" "
		  + "THEN sum(1.33*credits)/sum(credits) "
		  + "WHEN grade = \"D\" "
		  + "THEN sum(1*credits)/sum(credits) "
		  + "WHEN grade = \"D-\" "
		  + "THEN sum(.67*credits)/sum(credits) "
		  + "WHEN grade = \"F\" "
		  + "THEN \"0\" "
		  + "ELSE \"0\" "
		  + "END \"gpa\" "
		  + "FROM student NATURAL JOIN takes JOIN course "
		  + "GROUP BY student.ID");
    }
    
    public void printGPAInfo() throws IOException, SQLException
    {
		System.out.println("******** Query 1 ********");
    	while(resultSet.next())
    	{
    		int id = resultSet.getInt("ID");
    		String name = resultSet.getString("student.name");
    		double gpa = resultSet.getDouble("gpa");
    		System.out.println(id + " " + name + " " + gpa);
    	}
    }

    public void findMorningCourses() throws SQLException
    {
    	resultSet = statement.executeQuery("SELECT course_id, semester, year, sec_id " 
    			+ "FROM section NATURAL JOIN time_slot "
    			+ "WHERE start_hr <=12 "
    			+ "GROUP BY course_id");
    }

    public void printMorningCourses() throws IOException, SQLException
    {
		System.out.println("******** Query 2 ********");
    	while(resultSet.next())
    	{
    		String cid = resultSet.getString("course_id");
    		String sem = resultSet.getString("semester");
    		String year = resultSet.getString("year");
    		String sid = resultSet.getString("sec_id");
    		System.out.println(cid + " " + sem + " " + year + " " + sid);
    	}
    }

    public void findBusyInstructor() throws SQLException
    {
    	resultSet = statement.executeQuery("SELECT name " + "FROM instructor NATURAL JOIN teaches "
    									  + "GROUP BY name "
    									  + "HAVING COUNT(*) = (SELECT MAX(cnt) FROM ( SELECT COUNT(*) AS cnt FROM instructor NATURAL JOIN teaches GROUP BY ID) tmp ) "
    									  );
    }

    public void printBusyInstructor() throws IOException, SQLException
    {
		System.out.println("******** Query 3 ********");
		while(resultSet.next())
    	{
    		String tname = resultSet.getString("name");
    		System.out.println(tname);
    	}
    }

    public void findPrereq() throws SQLException
    {
    	resultSet = statement.executeQuery(" SELECT c1.title course, case when prereq_id is null then \'\' when prereq_id is not null "+
    			"then (select c2.title from course c2 where c2.course_id = prereq_id) end prereq "+
    			"from course c1 LEFT OUTER JOIN prereq  "+
    			"USING ( course_id );");
    }

    public void printPrereq() throws IOException, SQLException
    {
		System.out.println("******** Query 4 ********");
		while(resultSet.next())
    	{
			String course = resultSet.getString("course");
            String prereq = resultSet.getString("prereq");
            System.out.println(course+"   |   "+prereq);
    	}
    }

    public void updateTable() throws SQLException
    {
    	statement.executeUpdate("drop table if EXISTS studentCopy; ");
    	statement.executeUpdate("CREATE table studentCopy SELECT * from student;");
    	statement.executeUpdate("update studentCopy S set tot_cred = coalesce(( select sum(credits) "
    			+ "from takes natural join course "
    			+ "where S.ID= takes.ID and takes.grade <> \'F\' and takes.grade is not null), 0); ");
    	resultSet = statement.executeQuery("SELECT * FROM studentCopy; ");
    }

    public void printUpdatedTable() throws IOException, SQLException
    {
		System.out.println("******** Query 5 ********");
		 while (resultSet.next()) {
             String id = resultSet.getString("id");
             String name = resultSet.getString("name");
             String deptName = resultSet.getString("dept_name");
				String totCredit = resultSet.getString("tot_cred");
             System.out.println(id+" | "+name+" | "+deptName+" | "+totCredit);
     }
		
    }

    public void findFirstLastSemester() throws SQLException
    {
    	statement.executeUpdate("create temporary table firstLast select id, min(year) firstYear, max(year) lastYear from takes group by id;");
    	statement.executeUpdate("create temporary table firstYearSemesters "+
    			"select id, case when semester = \'Fall\' then 3 when semester = \'Summer\' then 2 when semester = \'Spring\' then 1 end semesterNumber "+
    			"from takes natural join firstLast where year = firstYear;");
    	statement.executeUpdate("create temporary table lastYearSemesters "+
    			"select id, case when semester = 'Fall' then 3 when semester = 'Summer' then 2 when semester = 'Spring' then 1 end semesterNumber "+
    			"from takes natural join firstLast where year = lastYear;");
    	resultSet = statement.executeQuery("select id, name, concat(case when min(f.semesterNumber) = 1 then 'Spring' when min(f.semesterNumber) = 2 "+
    			"then 'Summer' when min(f.semesterNumber) = 3 then 'Fall' end, ' ', cast(firstYear as char(4))) First_Semester, "+
    			"concat(case when max(l.semesterNumber) = 1 then 'Spring' when max(l.semesterNumber) = 2 "+
    			"then 'Summer' when max(l.semesterNumber) = 3 then 'Fall' end, ' ', cast(lastYear as char(4))) Last_Semester "+
    			"from firstYearSemesters f join firstLast using (id) join lastYearSemesters l using (id) join student using (id) "+
    			"group by id");
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
		Scanner kb = new Scanner(System.in);
		try
		{
			System.out.println("Please enter the department for the query: ");
			String deptName = kb.nextLine();
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
		catch(Exception err)
		{
			System.out.println("Err0r");
		}
	}
}






