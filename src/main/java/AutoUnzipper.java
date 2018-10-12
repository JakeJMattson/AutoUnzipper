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
						File newFile = new File(dir, newPath.toString());

						unArchive(newFile);
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

	private static void unArchive(File src) throws IOException
	{
		String[] split = src.getName().split("\\.", 2);

		if (split.length < 2)
			return;

		String extension = split[1];
		Archiver archiver;

		switch (extension)
		{
			case "zip":
				archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
				break;
			case "tar.gz":
				archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
				break;
			default:
				return;
		}

		File dst = new File(src.getParentFile(), split[0]);
		archiver.extract(src, dst);

		if (SHOULD_DELETE_ORIGINAL)
			src.delete();
	}
}