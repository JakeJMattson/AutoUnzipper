import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.nio.file.*;

class AutoUnzipper
{
	private static final boolean SHOULD_DELETE_ORIGINAL = true;

	public static void main(String[] args)
	{
		String downloads = System.getProperty("user.home") + "/Downloads/";
		watchDirectoryPath(new File(downloads));
	}

	private static void watchDirectoryPath(File dir)
	{
		Path path = dir.toPath();

		try (WatchService service = path.getFileSystem().newWatchService())
		{
			//Watch for creation events
			path.register(service, ENTRY_CREATE);

			while (true)
			{
				WatchKey key = service.take();

				for (WatchEvent<?> watchEvent : key.pollEvents())
					if (watchEvent.kind() == ENTRY_CREATE)
					{
						Path newPath = ((WatchEvent<Path>) watchEvent).context();
						File zip = new File(dir, newPath.toString());

						if (newPath.toString().endsWith("zip"))
							unzip(zip);
					}

				if (!key.reset())
					break;
			}
		}
		catch (IOException | ZipException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static void unzip(File zip) throws ZipException
	{
		String path = zip.getAbsolutePath();
		String destination = path.substring(0, path.lastIndexOf('.'));

		ZipFile zipFile = new ZipFile(zip);
		zipFile.extractAll(destination);

		if (SHOULD_DELETE_ORIGINAL)
			zip.delete();
	}
}