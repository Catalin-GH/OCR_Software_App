package connection;

import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Config_manager
{
	public static String file_name_and_type = null;
	public static String file_type = null;
	private String analyser_path = null;
	private String image_location = null;
	private String connection_file_type = null;
	private String download_get_arg = null;
	private String[] upload_post_args = null;
	
	Config_manager()
	{
		try 
		{
		    JSONParser jsonParser = new JSONParser();
			
			Object obj = jsonParser.parse(new FileReader(
					"C:/Workspace/OCR_Application/src/cfg/configuration.json"));
			JSONObject global = (JSONObject) obj;
			
			//Configuration - tools
			JSONObject tools = (JSONObject) global.get("tools");
            analyser_path = tools.get("analyser").toString();
            
            //Configuration - images_directory
			image_location = global.get("images_directory").toString();

			//Configuration - connection
			JSONObject connection = (JSONObject) global.get("connection");
			connection_file_type = connection.get("file_type").toString();
			download_get_arg = connection.get("download_arguments").toString();
			upload_post_args = new String[2];
			
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>) connection.get("upload_arguments");
            for (int i = 0; i < list.size(); i++) 
            {
            	upload_post_args[i] = list.get(i);
            }
		}  
		catch (Exception e) 
		{
            e.printStackTrace();
        }
	}
		
	public String getAnalyser_path() { return analyser_path; }
	public String getImage_location() { return image_location; }
	public String getConnection_file_type() { return connection_file_type; }
	public String getDownload_get_arg() { return download_get_arg; }
	public String[] getUpload_post_args() {	return upload_post_args; }
}
