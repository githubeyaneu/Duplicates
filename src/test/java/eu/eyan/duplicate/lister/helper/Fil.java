package eu.eyan.duplicate.lister.helper;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;


public class Fil implements Nameable{

	private String filName;
	private int sizeInBytes = 0;
	private int seed = 0;

	public Fil(String filName) {
		this.filName = filName;
	}

	public String getName() {
		return filName;
	}
	
	@Override
	public String toString() {
		return filName;
	}

	public Fil withRandomBinaryContent(int sizeInBytes, int seed) {
		this.sizeInBytes = sizeInBytes;
		this.seed = seed;
		return this;
	}

	public void build(File path) throws IOException {
		File d = new File(path, filName);
		d.createNewFile();
		if(sizeInBytes > 0){
			byte[] data = new byte[sizeInBytes];
			Random r = new Random(seed);
			r.nextBytes(data);
			FileUtils.writeByteArrayToFile(d, data);
		}
	}
}
