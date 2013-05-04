package eu.eyan.duplicate.lister;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import eu.eyan.duplicate.lister.helper.AbstractTreeTest;
import eu.eyan.duplicate.lister.helper.Dir;
import eu.eyan.duplicate.lister.helper.TreeBuilder;

public class TreeBuilderTest extends AbstractTreeTest {
	
	@Test
	public void buildTreeShouldWorkCorrect() {
		tree.delete();
		assertThat(new File(root)).doesNotExist();
		
		tree.build();
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

	
	@Test(timeout = 1000)
	public void buildTreeShouldNotAcceptCircledDirectories() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(TreeBuilder.CIRCLE_IN_DIRS_MESSAGE);
		Dir dir1 = new Dir("Dir1");
		Dir dir2 = new Dir("Dir2");
		Dir dir3 = new Dir("Dir3");
		dir1.withDir(dir2);
		dir2.withDir(dir3);
		dir3.withDir(dir1);
		TreeBuilder tree = new TreeBuilder(root)
								.withDir(dir1)
								.withDir(dir2)
								.withDir(dir3);

		tree.delete();
		tree.build();
	}
}
