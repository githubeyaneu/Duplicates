package eu.eyan.duplicate.lister.helper;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;


public class TreeBuilder extends Dir {

	public TreeBuilder(String dirPath) {
		super(dirPath);
	}

	public TreeBuilder build() throws IOException {
		validate();
		super.buildAsRoot();
		return this;
	}

	protected void validate() {
		assertThat(new File(getDirName())).as("Root " + getDirName() + " does exist!").doesNotExist();
		super.validate();
		//TODO filter for circles!
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
