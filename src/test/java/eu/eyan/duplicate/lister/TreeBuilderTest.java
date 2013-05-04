package eu.eyan.duplicate.lister;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import eu.eyan.duplicate.lister.helper.Dir;
import eu.eyan.duplicate.lister.helper.Fil;
import eu.eyan.duplicate.lister.helper.TreeBuilder;

public class TreeBuilderTest {

	private static final int FILE_CONTENT_SEED = 123;
	private static final int FILE_SIZE = 123;

	@Test
	public void buildTreeShouldWorkCorrect() {
		Fil sameFil = new Fil("same.binary").withRandomBinaryContent(FILE_SIZE, FILE_CONTENT_SEED);
		String root = "C:\\temp\\test";
		TreeBuilder tree = new TreeBuilder(root)
			.withDir(new Dir("1")
				.withDir(new Dir("11")
					.withFil(new Fil("11f1"))
					.withFil(new Fil("11f2"))
					.withFil(sameFil)
				)
				.withDir(new Dir("12"))
				.withFil(new Fil("1f1"))
				.withFil(new Fil("1f2"))
				.withFil(sameFil)
			)
			.withDir(new Dir("2")
				.withDir(new Dir("21"))
				.withDir(new Dir("22"))
				.withDir(new Dir("23"))
			)
			.withFil(sameFil);
		
		
		tree.delete();
		assertThat(new File(root)).doesNotExist();
		
		try {
			tree.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(3==3){
			
		}
		else{
			
		}
		
		assertThat(new File(root)).isDirectory();
		assertThat(new File(root+"\\1")).isDirectory();
		assertThat(new File(root+"\\1\\11")).isDirectory();
		assertThat(new File(root+"\\1\\11\\11f1")).isFile();
		assertThat(new File(root+"\\1\\11\\11f1")).hasSize(0);
		assertThat(new File(root+"\\1\\11\\11f2")).isFile();
		assertThat(new File(root+"\\1\\11\\same.binary")).isFile();
		assertThat(new File(root+"\\1\\11\\same.binary")).hasSize(FILE_SIZE);
		assertThat(new File(root+"\\1\\12")).isDirectory();
		assertThat(new File(root+"\\1\\1f1")).isFile();
		assertThat(new File(root+"\\1\\1f2")).isFile();
		assertThat(new File(root+"\\1\\same.binary")).isFile();
		assertThat(new File(root+"\\1\\same.binary")).hasSize(FILE_SIZE);
		assertThat(new File(root+"\\1\\same.binary")).hasSameContentAs(new File(root+"\\1\\11\\same.binary"));
		assertThat(new File(root+"\\2")).isDirectory();
		assertThat(new File(root+"\\2\\21")).isDirectory();
		assertThat(new File(root+"\\2\\22")).isDirectory();
		assertThat(new File(root+"\\2\\23")).isDirectory();
		assertThat(new File(root+"\\same.binary")).isFile();
	}

}
