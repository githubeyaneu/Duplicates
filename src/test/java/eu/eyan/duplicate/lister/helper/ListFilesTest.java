package eu.eyan.duplicate.lister.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class ListFilesTest extends AbstractTreeTest{

	@Test
	public void listFilesHasToWorkCorrect() {
		tree.delete();
		tree.build();
		
	}

}
