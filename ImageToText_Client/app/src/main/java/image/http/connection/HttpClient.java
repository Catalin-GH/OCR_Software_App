package image.http.connection;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient
{
	private String url;
    private HttpURLConnection con;
    private OutputStream os;
    
	private String delimiter = "--";
    private String boundary =  "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";

	public HttpClient(String url) {		
		this.url = url;
	}

	public byte[] downloadImage()
	{
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		try
		{
			HttpURLConnection con = (HttpURLConnection) ( new URL(url)).openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.connect();

			InputStream is = con.getInputStream();
			BufferedInputStream in = new BufferedInputStream(is);

			byte[] b = new byte[2];

			while ( in.read(b) != -1)
			{
				byteArrayOS.write(b);
			}

			con.disconnect();

		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}

		return byteArrayOS.toByteArray();
	}

	public void connectForMultipart() throws Exception
	{
		con = (HttpURLConnection) ( new URL(url)).openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		con.connect();
		os = con.getOutputStream();
	}
	
	public void addFormPart(String paramName, String value) throws Exception
	{
		writeParamData(paramName, value);
	}
	
	public void addFilePart(String paramName, String fileName, byte[] data) throws Exception
	{
		os.write( (delimiter + boundary + "\r\n").getBytes());
		os.write( ("Content-Disposition: form-data; name=\"" + paramName +
				"\"; filename=\"" + fileName + "\"\r\n" ).getBytes());
		os.write( ("Content-Type: application/octet-stream\r\n").getBytes());
		os.write( ("Content-Transfer-Encoding: binary\r\n").getBytes());
		os.write("\r\n".getBytes());
		os.write(data);
		os.write("\r\n".getBytes());
	}
	
	public void finishMultipart() throws Exception
	{
		os.write( (delimiter + boundary + delimiter + "\r\n").getBytes());
	}
	
	
	public String getResponse() throws Exception
	{
		InputStream is = con.getInputStream();
		byte[] buffer = new byte[1024];
		StringBuilder sb = new StringBuilder();
		
		while ( is.read(buffer) != -1)
			sb.append(new String(buffer));
		
		con.disconnect();
		
		return buffer.toString();
	}
	
	private void writeParamData(String paramName, String value) throws Exception
	{
		os.write( (delimiter + boundary + "\r\n").getBytes());
		os.write( "Content-Type: text/plain\r\n".getBytes());
		os.write( ("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
		os.write( ("\r\n" + value + "\r\n").getBytes());
	}
}
