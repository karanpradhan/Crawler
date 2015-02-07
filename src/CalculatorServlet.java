import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
public class CalculatorServlet extends HttpServlet{

	public void doGet(HttpServletRequest request,HttpServletResponse response) throws java.io.IOException{
		int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
		int v2 = Integer.valueOf(request.getParameter("num1")).intValue();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Hello</title></head>");
		out.println("<body>Addition is "+(v1+v2)+"</body></html>");
	}
}
