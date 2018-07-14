package connection;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/OCR")
@MultipartConfig
public class Connection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Config_manager config = null;
	private String imagePath = null;
	private String message = "";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("A fost apelata metoda doGET");

		sendMessage(message, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("A fost apelata metoda doPOST");
		
		config = new Config_manager();
		
		saveFile(request);
		
		imageAnalyser();
		
		message = readMessage();
		
		OutputStream outputMessage = response.getOutputStream();
		outputMessage.write("Procesarea s-a terminat! Apasati butonul \"Descarca\".".getBytes());
		outputMessage.close();    
	}
	
	private void imageAnalyser() {
		 try {
		      Process p = Runtime.getRuntime().exec("python " + config.getAnalyser_path() + " -i " + Unique.ID + Config_manager.file_type);
		      p.waitFor();
		 }
		 catch (Exception err) {
		      err.printStackTrace();
		 }
	}
	
	private String readMessage() throws IOException
	{
		String result = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(config.getImage_location() + Unique.ID + "/" + Unique.ID + ".txt"));
		    StringBuilder sb = new StringBuilder();
		    
		    String line = br.readLine();
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    result = sb.toString();
		    br.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	
		return result;
	}
	
    private void saveFile(HttpServletRequest request) {
    	try {
        	//get file name and type
			Part p = request.getPart(config.getConnection_file_type());
			
			//keep original name
			Config_manager.file_name_and_type = extractFileNameAndType(p);
			Config_manager.file_type = getFileType(Config_manager.file_name_and_type);	
				
			//generate ID_Unique
			Unique.ID = Unique.generateID();
					
			//create a unique directory to store the image
			File directory = new File(config.getImage_location() + Unique.ID);
			if(!directory.exists())
			{
				directory.mkdir();
			}
			
			//save file with an unique ID
			imagePath = config.getImage_location() + Unique.ID + "/" +
					Unique.ID + Config_manager.file_type;
			
			File f = new File(imagePath);
			
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = p.getInputStream();
			byte[] buffer = new byte[1024];

			while (is.read(buffer) != -1) {
				fos.write(buffer);
			}
			fos.close();
			is.close();
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
	
	private String getFileType(String fileNameAndType)
	{
		StringBuilder sb = new StringBuilder(fileNameAndType);
		int index = sb.lastIndexOf(".");
		sb.delete(0, index);
		return sb.toString();
	}
	
	private void sendMessage(String message, HttpServletResponse response) {	
		try {
			response.setContentLength(message.length());
			OutputStream os = response.getOutputStream();
			os.write(message.getBytes());
			os.flush();
			os.close();
			System.out.println("A fost trimis mesajul: " + message);
		} 
		catch (Exception e) {
			System.out.println("Exceptie la trimiterea mesajului: " + message);
			e.printStackTrace();
		}	
	}
	
	private void sendImage(File file, HttpServletResponse response) {
		BufferedImage image;
		try {
			image = ImageIO.read(file);
			response.setContentLength((int) file.length());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			OutputStream os = response.getOutputStream();
			os.write(baos.toByteArray());
			os.close();
			System.out.println("A fost trimis fisierul: " + Unique.ID + Config_manager.file_type);
		} 
		catch (IOException e) {
			System.out.println("Exceptie la trimiterea fisierului: " + Unique.ID + Config_manager.file_type);
			e.printStackTrace();
		}
	}
	
}