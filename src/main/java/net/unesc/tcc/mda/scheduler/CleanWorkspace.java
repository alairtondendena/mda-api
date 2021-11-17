package net.unesc.tcc.mda.scheduler;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CleanWorkspace {

	@Scheduled(fixedDelay = 10000)
	public void execute() {
		long currentTime = Instant.now().toEpochMilli();
		ArrayList<String> paths = new ArrayList<>();
		paths.add("target" + File.separator + "start-spring" + File.separator);
		paths.add("target" + File.separator + "generated-mda-projects" + File.separator);
		paths.add("target" + File.separator + "mda-projects" + File.separator);
		for (String path : paths) {
			File folder = new File(path);
			if (Objects.isNull(folder.listFiles())) {
				return;
			}
			for (File file : folder.listFiles()) {
				if (currentTime > Long.parseLong(file.getName().split("-")[0])) {
					try {
						if (file.isDirectory()) {
							log.info(file.getName());
							FileUtils.deleteDirectory(file);
							continue;
						}
						FileUtils.delete(file);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}
	}

}
