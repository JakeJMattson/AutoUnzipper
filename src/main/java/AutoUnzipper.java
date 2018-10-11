import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import org.rauschig.jarchivelib.*;

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
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static void unzip(File src) throws IOException
	{
		String path = src.getAbsolutePath();
		File dst = new File(path.substring(0, path.lastIndexOf('.')));

		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
		archiver.extract(src, dst);

		if (SHOULD_DELETE_ORIGINAL)
			src.delete();
	}
}