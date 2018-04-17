package connection.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/UploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String fileNameAndType = null;
	private String fileName = null;
	
//	nu este necesara
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//			throws ServletException, IOException {
//		System.out.println("UploadServer.java [doGet]" + fileNameAndType);
//	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		upload(request,response);
		System.out.println("UploadServer.java [doPost]" + fileNameAndType);
	}
	
	private void upload(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException 
	{
	try {
			String[] param = new String[Config.uploadPOSTArgs.length];
			for (int i = 0; i < Config.uploadPOSTArgs.length; i++) 
			{
				param[i] = request.getParameter(Config.uploadPOSTArgs[i]);
				System.out.println("param " + (i + 1) + " = [ " + param[i] + " ]");
			}

			Part p = request.getPart(Config.fileType);
			fileNameAndType = extractFileNameAndType(p);
			fileName = getFileName(fileNameAndType);
			Config.fileName = fileName;
			Config.fileNameAndType = fileNameAndType;
			File f = new File(Config.imageLocation + fileNameAndType);
			
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = p.getInputStream();
			byte[] buffer = new byte[1024];

			while (is.read(buffer) != -1)
			{
				fos.write(buffer);
			}

			is.close();
			fos.close();
			
			OutputStream os = response.getOutputStream();
			os.write("Fisierul a fost trimis!".getBytes());
			os.close();
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private String extractFileNameAndType(Part part) {
	    String contentDisp = part.getHeader("content-disposition");
	    String[] items = contentDisp.split(";");
	    for (String s : items) {
	        if (s.trim().startsWith("filename")) {
	            return s.substring(s.indexOf("=") + 2, s.length()-1);
	        }
	    }
	    return "";
	}
	
	private String getFileName(String fileNameAndType)
	{
		StringBuilder sb = new StringBuilder(fileNameAndType);
		int index = sb.lastIndexOf(".");
		sb.delete(index, sb.length());
		return sb.toString();
	}
}