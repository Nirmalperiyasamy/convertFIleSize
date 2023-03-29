package com.hriday.convertFileSize;

import org.hriday.archiveFile.ArchiveFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConvertFileSizeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConvertFileSizeApplication.class, args);
	}

	@Bean
	public ArchiveFile archiveFile(){
		return new ArchiveFile();
	}

}
