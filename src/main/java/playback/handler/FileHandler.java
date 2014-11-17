package playback.handler;

import httpserver.HTTPException;
import httpserver.HTTPRequest;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import playback.PlaybackSessionServer;

public class FileHandler extends StorytellerHTTPHandler
{

	public static final String STATIC_FILES_DIRECTORY = "frontend";
	public static final String MESSAGE_404 = "404 - File Not Found";

	public FileHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException
	{
		super(request, sessionManager);
	}

	@Override
	public void handle() throws HTTPException
	{
		try
		{
			// Create the path
			StringBuilder pathBuilder = new StringBuilder();

			// Add a '/' and part of our path
			for (String segment : getRequest().getSplitPath())
			{
				pathBuilder.append("/");
				pathBuilder.append(segment);
			}

			// Set the path to the pathBuilder or a '/' if the path is empty.
			String path = pathBuilder.toString();
			if (path.isEmpty())
			{
				path = "/";
			}

			// If the path ends in a '/' append `playback.html`
			if (path.substring(path.length() - 1).equals("/"))
			{
				path += "playback.html";
			}

			path = STATIC_FILES_DIRECTORY + path;

			// check that file exists
			File f;
			try
			{
				f = new File(getResource(path));
				if (!f.exists())
				{
					throw new NullPointerException("No such file: " + path);
				}
			}
			catch (NullPointerException e)
			{
				message(404, MESSAGE_404);
				return;
			}

			setResponseType(getResponseType(f));

			// If its an impage or zip we have to read the file differently.
			if (isImageResponse() || isZipResponse())
			{
				setResponseText(f.toURI().toURL().toString());
				setResponseSize(new File(new URL(getResponseText()).toString())
						.length());
				return;
			}

			// Read the file
			InputStream inputStream = ClassLoader
					.getSystemResourceAsStream(path);

			// If the file doesn't exist, tell the client.
			if (inputStream == null)
			{
				message(404, MESSAGE_404);
				return;
			}

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			StringBuilder builder = new StringBuilder();

			for (String line = bufferedReader.readLine(); line != null; line = bufferedReader
					.readLine())
			{
				builder.append(line);
				builder.append("\n");
			}

			bufferedReader.close();

			// Set the response to the file's contents.
			setResponseText(builder.toString());

			setHandled(true);
		}
		catch (IOException e)
		{
			throw new HTTPException("File Not Found", e);
		}
	}

	public boolean isImageResponse()
	{
		return getResponseType().contains("image")
				&& !getResponseType().contains("svg+xml")
				&& !getResponseType().contains("icon");
	}

	public boolean isZipResponse()
	{
		return getResponseType().equalsIgnoreCase("application/zip");
	}

	@Override
	public void writeData() throws IOException
	{
		if (isImageResponse() && getResponseCode() != 404)
		{
			String imgType = getResponseType().substring(
					getResponseType().length() - 3);
			BufferedImage img = ImageIO.read(new URL(getResponseText())
					.openStream());
			ImageIO.write(img, imgType, getWriter());
		}

		else if (isZipResponse() && getResponseCode() != 404)
		{
			String fileLocation = getResponseText().substring("file:".length());
			ZipFile zipFile = new ZipFile(fileLocation);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			ZipOutputStream zipOut = new ZipOutputStream(getWriter());

			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				zipOut.putNextEntry(entry);
				InputStream reader = zipFile.getInputStream(entry);
				for (int i = 0; i < entry.getSize(); i++)
				{
					zipOut.write(reader.read());
				}
			}

			zipOut.finish();
			zipFile.close();

			// If we are sending a zip file, it is the exported files. We want
			// to delete it
			// after it is requested and served.
			new File(fileLocation).delete();

			// new File(fileLocation).delete();
		}
		else
		{
			writeLine(getResponseText());
		}
	}

	public String getResponseType(File f)
	{
		try
		{
			String probeType = Files.probeContentType(f.toPath());
			if (probeType != null)
			{
				return probeType;
			}

		}
		catch (IOException e)
		{
			// whatevs ...
			// If we get an IOException here, we can just try manually...
		}

		String path = f.toString();

		if (path.substring(path.length() - 4).equalsIgnoreCase("html"))
		{
			return "text/html";
		}
		else if (path.substring(path.length() - 3).equalsIgnoreCase("css"))
		{
			return "text/css";
		}
		else if (path.substring(path.length() - 2).equalsIgnoreCase("js"))
		{
			return "text/javascript";
		}
		else if (path.substring(path.length() - 3).equalsIgnoreCase("png"))
		{
			return "image/png";
		}
		else if (path.substring(path.length() - 3).equalsIgnoreCase("jpg"))
		{
			return "image/jpg";
		}
		else if (path.substring(path.length() - 3).equalsIgnoreCase("svg"))
		{
			return "image/svg+xml";
		}
		else if (path.substring(path.length() - 3).equalsIgnoreCase("zip"))
		{
			return "application/zip";
		}
		else
		{
			return "text/plain";
		}
	}

}
