package eu.eyan.duplicate.lister.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fest.assertions.Assertions;

import com.google.common.collect.Lists;

public class Dir implements Nameable {

	private String dirName;
	private List<Dir> myDirs = Lists.newArrayList();
	private List<Fil> myFils = Lists.newArrayList();

	public Dir(String dirName) {
		this.dirName = dirName;
	}

	public Dir withDir(Dir dir) {
		myDirs.add(dir);
		return this;
	}

	public Dir withFil(Fil fil) {
		myFils.add(fil);
		return this;
	}

	protected String getDirName() {
		return dirName;
	}

	protected void validate() {
		validateNoDuplicates(myDirs);
		validateNoDuplicates(myFils);
		for (Dir dir : myDirs) {
			dir.validate();
		}
	}

	private void validateNoDuplicates(List<?> list) {
		Set<String> set = new TreeSet<String>();
		for (Object o : list) {
			set.add(((Nameable)o).getName());
		}
		Assertions.assertThat(set.size()).as("There are duplicates in list (from Dir " + dirName + "): "+list).isEqualTo(list.size());
	}

	public String getName() {
		return dirName;
	}
	
	@Override
	public String toString() {
		return dirName;
	}

	private void build(File path) throws IOException {
		for (Dir dir : myDirs) {
			File d = new File(path, dir.dirName);
			d.mkdir();
			dir.build(d);
		}
		for (Fil fil : myFils) {
			fil.build(path);
		}
	}

	public void buildAsRoot() throws IOException {
		File root = new File(dirName);
		root.mkdirs();
		build(root);
	}
}
