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
	private List<Dir> subDirs = Lists.newArrayList();
	private List<Fil> fils = Lists.newArrayList();

	public Dir(String dirName) {
		this.dirName = dirName;
	}

	public Dir withDir(Dir dir) {
		subDirs.add(dir);
		return this;
	}

	public Dir withFil(Fil fil) {
		fils.add(fil);
		return this;
	}

	protected String getDirName() {
		return dirName;
	}

	protected void validate() {
		validateNoDuplicates(subDirs);
		validateNoDuplicates(fils);
		for (Dir dir : subDirs) {
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
		for (Dir dir : subDirs) {
			File d = new File(path, dir.dirName);
			d.mkdir();
			dir.build(d);
		}
		for (Fil fil : fils) {
			fil.build(path);
		}
	}

	protected void buildAsRoot() throws IOException {
		File root = new File(dirName);
		root.mkdirs();
		build(root);
	}

	protected List<Dir> getSubDirs() {
		return subDirs;
	}
}
