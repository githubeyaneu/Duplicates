package eu.eyan.duplicate;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;

//import eu.eyan.duplicate.lister.helper.Fil;

public class DuplicateFinder {

	public static void main(String[] args) {
		// File[] roots = File.listRoots();
		File[] roots = { new File("C:") };
		System.out.println("Searching: " + Arrays.toString(roots));
		FileFilter sizeFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.length() >= 1024 * 1024 * 1024;
			}
		};
		Collection<Fil> files = listFiles(roots, sizeFilter);
		Collection<Collection<Fil>> duplicates = findDuplicates(files);
	}

	private static Collection<Collection<Fil>> findDuplicates(Collection<Fil> files) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Collection<Fil> listFiles(File[] roots, FileFilter filter) {
		Collection<Fil> ret = newArrayList();
		for (File root : roots) {
			System.out.println("Searching: " + root);
			Collection<Fil> files = listDir(root, filter);
			ret.addAll(files);
		}
		return ret;
	}

	private static Collection<Fil> listDir(File dir, FileFilter filter) {
		System.out.println("list " + dir.getAbsolutePath());
		Collection<Fil> ret = newArrayList();
		File[] filesAndDirs = dir.listFiles(filter);
		for (int i = 0; i < filesAndDirs.length; i++) {
			File fd = filesAndDirs[i];
			System.out.println("anal " + fd);
			if (fd.isDirectory()) {
				ret.addAll(listDir(fd, filter));
			} else {
				// ret.add(new Fil(fd.getAbsolutePath()));
			}
		}
		return ret;
	}
}
