package connection.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String fileNameAndType = null;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException
	{
		fileNameAndType = Config.fileNameAndType;
		
		String pythonScriptPath = "C:/Workspace/Licenta/ImageAnalyser/test.py";
		String[] cmd = new String[3];
		cmd[0] = "python";
		cmd[1] = pythonScriptPath;
		cmd[2] = fileNameAndType;
		String command = "";
		for(int i = 0; i < cmd.length; i++)
		{
			command += (cmd[i] + " ");
		}
		
		Process p = Runtime.getRuntime().exec(command);
		try 
		{
			p.waitFor();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		downloadData(request, response);
		System.out.println("DownloadServer.java [doGet]" + fileNameAndType);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		System.out.println("DownloadServer.java [doPost]" + fileNameAndType);
	}
	
	private void downloadData(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException 
	{
		String imgPath = Config.imageLocation + fileNameAndType;
		File f = new File(imgPath);
		FileInputStream fis = null;
		
		byte[] buffer = new byte[1024];
		
		response.setContentLength((int) f.length());
		try 
		{
			fis = new FileInputStream(f);
			OutputStream os = response.getOutputStream();
			while ( fis.read(buffer) != -1) 
			{				
				os.write(buffer);
			}
			
			System.out.println("A fost trimis fisierul: " + fileNameAndType);
		} 
		catch (org.apache.catalina.connector.ClientAbortException cae) 
		{
		    System.out.println("Abrorting transfer. it happens, no problem");
		    cae.printStackTrace();
		  
		} 
		catch (IOException ioe) 
		{
			 System.out.println("i/o exception raised.  abrorting.");
			 ioe.printStackTrace();
		}
		finally 
		{
			try 
			{
				fis.close(); 
			} 
			catch(Throwable t) 
			{}
		}
	}
}