package eu.eyan.duplicate.lister.helper;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;


public class TreeBuilder extends Dir {

	public static final String CIRCLE_IN_DIRS_MESSAGE = "There is a circle in the directory structure!";

	public TreeBuilder(String dirPath) {
		super(dirPath);
	}

	public TreeBuilder build() {
		validate();
		try {
			super.buildAsRoot();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return this;
	}
	
	protected void validate() {
		verifyNoCirles(this, null);
		assertThat(new File(getDirName())).as("Root " + getDirName() + " does exist!").doesNotExist();
		super.validate();
	}

	private void verifyNoCirles(Dir dir, List<Dir> path) {
		if(null==path){
			path = Lists.newArrayList();
		}
		path.add(dir);
		for (Dir subDir : dir.getSubDirs()) {
			if(path.contains(subDir))
			{
				path.add(subDir);
				Exception e = new Exception("Circle: " + path);
				throw new IllegalStateException(CIRCLE_IN_DIRS_MESSAGE, e);
			}
			verifyNoCirles(subDir, path);
		}
		assertThat(path.get(path.size()-1)).isSameAs(dir);
		path.remove(dir);
	}

	public TreeBuilder withDir(Dir dir) {
		super.withDir(dir);
		return this;
	}

	public TreeBuilder withFil(Fil fil) {
		super.withFil(fil);
		return this;
	}

	public boolean delete() {
		File file = new File(getDirName());
		assertThat(getDirName()).as("Should play just in temp!").containsIgnoringCase("temp");
		try {
			FileUtils.deleteDirectory(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
