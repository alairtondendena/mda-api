package net.unesc.tcc.mda.generators;

import com.squareup.javapoet.JavaFile;
import net.unesc.tcc.mda.core.MdaModel;

public interface MdaGenerator {
	JavaFile generate(String packageName, MdaModel model);
}
